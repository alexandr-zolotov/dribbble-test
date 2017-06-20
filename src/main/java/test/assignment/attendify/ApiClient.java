package test.assignment.attendify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ApiClient {

  private final String getProfilePath = "users/{user}";
  private final String getFollowersPath = "users/{user}/followers";
  private final String getShotsPath = "users/{user}/shots";
  private final String getShotLikesPath = "/shots/{id}/likes";

  private final String baseUrl;
  private final int maxPageSize;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestTemplate restTemplate;
  private final ExecutorService httpExecutor;

  public ApiClient() {

    Config config = ConfigFactory.load().getConfig("dribbble.api");

    baseUrl = config.getString("baseUrl");
    String clientAccessToken = config.getString("clientAccessToken");

    ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
      request.getHeaders().add("Authorization", "Bearer " + clientAccessToken);
      return execution.execute(request, body);
    };

    restTemplate = new RestTemplate();

    restTemplate.setInterceptors(Collections.singletonList(authInterceptor));
    restTemplate.setMessageConverters(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));

    maxPageSize = config.getInt("maxPageSize");

    httpExecutor= new ForkJoinPool();
    Runtime.getRuntime().addShutdownHook(new Thread(httpExecutor::shutdownNow));
  }


  public User getUserInfo(String userName) {
    URI uri = UriComponentsBuilder.fromUriString(baseUrl + getProfilePath).buildAndExpand(userName).toUri();
    return restTemplate.getForObject(uri, User.class);
  }

  public CompletableFuture<List<User>> getFollowers(User user) {
    long followersCount = user.getFollowersCount();
    return collectPaged(page -> getFollowers(user.getUserName(), page), followersCount);
  }

  public CompletableFuture<List<Shot>> getShots(User user) {
    return collectPaged(page -> getShots(user.getUserName(), page), user.getShotsCount());
  }

  public CompletableFuture<List<User>> getWhoLiked(Shot shot) {
    return collectPaged(page -> getWhoLiked(shot.getId(), page), shot.getLikesCount());
  }


  private <T> CompletableFuture<List<T>> collectPaged(Function<Page, List<T>> pageHandler, long itemsCount) {

    int pagesCount = (int) Math.ceil(((double) itemsCount) / maxPageSize);

    List<CompletableFuture<List<T>>> pagedFollowers = IntStream.range(0, pagesCount)
            .limit(pagesCount)
            .mapToObj(pageNumber ->
                    CompletableFuture.supplyAsync(
                            () -> pageHandler.apply(new Page(pageNumber, maxPageSize)),
                            httpExecutor))
            .collect(Collectors.toList());

    return Util.sequence(pagedFollowers);
  }


  private List<Shot> getShots(String userName, Page page) {
    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put("user", userName);
    JsonNode jsonNode = getPage(getShotsPath, queryParams, page);
    return toList(jsonNode, Shot.class);
  }

  private List<User> getWhoLiked(long shotId, Page page){
    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put("id", shotId);
    JsonNode jsonNode = getPage(getShotLikesPath, queryParams, page);
    return toList(jsonNode, "user", User.class);
  }

  private List<User> getFollowers(String userName, Page page) {

    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put("user", userName);

    JsonNode resultPage = getPage(getFollowersPath, queryParams, page);
    return toList(resultPage, "follower", User.class);
  }

  private <T> List<T> toList(JsonNode source, Class<T> listItemClass) {
    return toList(source, null, listItemClass);
  }

  private <T> List<T> toList(JsonNode source, String path, Class<T> listItemClass) {
    if (!source.isArray()) {
      throw new RuntimeException("Unexpected API response body format. Response body: " + source.toString());
    }

    Spliterator<JsonNode> spliterator = Spliterators.spliteratorUnknownSize(source.iterator(), Spliterator.CONCURRENT);
    Stream<JsonNode> nodesStream =
            StringUtils.isEmpty(path)
                    ? StreamSupport.stream(spliterator, true)
                    : StreamSupport.stream(spliterator, true).map(jsonNode -> jsonNode.path(path));

    List<T> result = nodesStream.map(jsonNode -> {

      try {
        return objectMapper.readerFor(listItemClass).<T>readValue(jsonNode);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }).collect(Collectors.toList());

    return result;
  }

  private JsonNode getPage(String path, Map<String, Object> pathVars, Page page) {
    URI uri = UriComponentsBuilder
            .fromUriString(baseUrl + path)
            .queryParam("page", page.number)
            .queryParam("per_page", page.size)
            .buildAndExpand(pathVars).toUri();
    return restTemplate.getForObject(uri, JsonNode.class);
  }

  private class Page {

    final int number;
    final int size;

    Page(int number, int size) {
      this.number = number;
      this.size = size;
    }
  }

}
