package test.assignment.attendify;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.isNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Shot {

  private long id;
  private long likesCount;

  @JsonCreator
  public Shot(@JsonProperty("id") long id, @JsonProperty("likes_count") long likesCount) {
    this.id = id;
    this.likesCount = likesCount;
  }

  public long getLikesCount() {
    return likesCount;
  }

  public long getId() {
    return id;
  }

  @Override
  public boolean equals(Object other) {
    return !isNull(other)
            && other instanceof Shot
            && ((Shot) other).id == this.id;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
