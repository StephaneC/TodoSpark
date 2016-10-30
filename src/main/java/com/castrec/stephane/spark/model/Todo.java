package com.castrec.stephane.spark.model;

/**
 * Created by sca on 29/10/16.
 */

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class Todo {

  public enum Status {
    ACTIVE,
    COMPLETE
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  String title;
  String id;
  Status status;

  public void toggleStatus() {
    this.status = isComplete() ? Status.ACTIVE : Status.COMPLETE;
  }

  public boolean isComplete() {
    return this.status == Status.COMPLETE;
  }

  public static Todo create(String title) {
    return new Todo(title, UUID.randomUUID().toString(), Status.ACTIVE);
  }

}