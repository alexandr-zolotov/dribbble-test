package test.assignment.attendify;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DribbbleStats {

  public static void main(String[] args) {

    if(args.length != 1) {
      System.out.println("Exactly one user name expected");
      return;
    }

    String userName = args[0];

    ApiClient client = new ApiClient();
    User user = client.getUserInfo(userName);

    CompletableFuture<List<User>> followers = client.getFollowers(user);

    Map<User, Long> userLikesCount =
            followers.thenApply(allFollowers -> Util.sequence(allFollowers.stream().map(client::getShots).collect(Collectors.toList())))
                    .join()
                    .thenApply(allShots -> Util.sequence(allShots.stream().map(client::getWhoLiked).collect(Collectors.toList())))
                    .join()
                    .thenApply(likers -> likers.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())))
                    .join();


    Map<User, Long> topLikers = userLikesCount.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
            .limit(10)
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
            ));

    topLikers.forEach((liker, score) -> System.out.println(liker.getName()));
  }
}
