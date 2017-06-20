package test.assignment.attendify;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Util {

  public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<List<T>>> futures) {
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
            .thenApply(aVoid -> futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList())
            );
  }
}
