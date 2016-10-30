package com.castrec.stephane.spark;

import com.castrec.stephane.spark.model.Todo;

import spark.*;
import spark.template.velocity.*;
import java.util.*;
import static spark.Spark.*;

/**
 * Created by sca on 29/10/16.
 */
public class Application {

  public static void main(String[] args) {

    exception(Exception.class, (e, req, res) -> e.printStackTrace()); // print all exceptions
    staticFiles.location("/public");
    port(9999);


    /**
     * View part
     */
    get("/", (req, res) -> renderTodos(req));
    get("/todos/:id/edit", (req, res) -> renderEditTodo(req));

    post("/todos", (ICRoute) (req) -> TodoDao.add(Todo.create(req.queryParams("todo-title"))));
    delete("/todos/completed", (ICRoute) (req) -> TodoDao.removeCompleted());
    delete("/todos/:id", (ICRoute) (req) -> TodoDao.remove(req.params("id")));
    put("/todos/toggle_status", (ICRoute) (req) -> TodoDao.toggleAll(req.queryParams("toggle-all") != null));
    put("/todos/:id", (ICRoute) (req) -> TodoDao.update(req.params("id"), req.queryParams("todo-title")));
    put("/todos/:id/toggle_status", (ICRoute) (req) -> TodoDao.toggleStatus(req.params("id")));

    /**
     * REST Part
     */
    JSonTransformer jsonT = new JSonTransformer();
    get("/json", (req, res) -> TodoDao.all(), jsonT);
    get("/json/todos/:id", (req, res) -> TodoDao.find(req.params("id")));

    post("/json/todos", (ICRoute) (req) -> TodoDao.add(Todo.create(req.queryParams("todo-title"))), jsonT);
    delete("/json/todos/completed", (ICRoute) (req) -> TodoDao.removeCompleted());
    delete("/json/todos/:id", (ICRoute) (req) -> TodoDao.remove(req.params("id")));
    put("/json/todos/toggle_status", (ICRoute) (req) -> TodoDao.toggleAll(req.queryParams("toggle-all") != null));
    put("/json/todos/:id", (ICRoute) (req) -> TodoDao.update(req.params("id"), req.queryParams("todo-title")));
    put("/json/todos/:id/toggle_status", (ICRoute) (req) -> TodoDao.toggleStatus(req.params("id")));


    after((req, res) -> {
      if (res.body() == null && !req.contextPath().contains("/json")) { // if the route didn't return anything or json path
        res.body(renderTodos(req));
      }
    });
  }

  private static String renderTodos(Request req) {
    String statusStr = req.queryParams("status");
    Map<String, Object> model = new HashMap<>();
    model.put("todos", TodoDao.ofStatus(statusStr));
    model.put("filter", Optional.ofNullable(statusStr).orElse(""));
    model.put("activeCount", TodoDao.ofStatus(Todo.Status.ACTIVE).size());
    model.put("anyCompleteTodos", TodoDao.ofStatus(Todo.Status.COMPLETE).size() > 0);
    model.put("allComplete", TodoDao.all().size() == TodoDao.ofStatus(Todo.Status.COMPLETE).size());
    model.put("status", Optional.ofNullable(statusStr).orElse(""));
    if ("true".equals(req.queryParams("ic-request"))) {
      return renderTemplate("velocity/todoList.vm", model);
    }
    return renderTemplate("velocity/index.vm", model);
  }

  private static String renderTemplate(String template, Map model) {
    return new VelocityTemplateEngine().render(new ModelAndView(model, template));
  }
  private static String renderEditTodo(Request req) {
    return renderTemplate("velocity/editTodo.vm", new HashMap(){{ put("todo", TodoDao.find(req.params("id"))); }});
  }

  @FunctionalInterface
  private interface ICRoute extends Route {
    default Object handle(Request request, Response response) throws Exception {
      handle(request);
      return "";
    }
    void handle(Request request) throws Exception;
  }

}
