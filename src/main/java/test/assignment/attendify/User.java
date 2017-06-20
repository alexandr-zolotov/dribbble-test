package test.assignment.attendify;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.isNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

  private final long id;
  private final String userName;
  private final String name;
  private final long followersCount;
  private final long shotsCount;

  @JsonCreator
  public User(@JsonProperty("id") long id,
              @JsonProperty("username") String userName,
              @JsonProperty("name") String name,
              @JsonProperty("followers_count") long followersCount,
              @JsonProperty("shots_count") long shotsCount) {
    this.id = id;
    this.userName = userName;
    this.name = name;
    this.followersCount = followersCount;
    this.shotsCount = shotsCount;
  }

  public String getUserName() {
    return userName;
  }

  public String getName() {
    return name;
  }

  public long getFollowersCount() {
    return followersCount;
  }

  public long getShotsCount() {
    return shotsCount;
  }

  public long getId() {
    return id;
  }

  public boolean equals(Object other){
    return !isNull(other)
            && other instanceof User
            && ((User) other).id == this.id;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  @Override
  public String toString() {
    return "User{" +
            "id=" + id +
            ", userName='" + userName + '\'' +
            ", followersCount=" + followersCount +
            '}';
  }
}
