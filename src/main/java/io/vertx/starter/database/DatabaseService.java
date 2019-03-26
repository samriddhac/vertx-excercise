package io.vertx.starter.database;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * The ProxyGen annotation is used to trigger the code generation of a proxy for clients of that service.
 * The Fluent annotation is optional, but allows fluent interfaces where operations can be chained by returning the service instance. 
 * This is mostly useful for the code generator when the service shall be consumed from other JVM languages.
 * 
 * Parameter types need to be strings, Java primitive types, 
 * JSON objects or arrays, any enumeration type or a java.util collection (List / Set / Map) of the previous types. 
 * The only way to support arbitrary Java classes is to have them as Vert.x data objects, annotated with @DataObject.
 * 
 * Since services provide asynchronous results, the last argument of a service method needs to be a Handler<AsyncResult<T>>
 * 
 */
@ProxyGen
@VertxGen
public interface DatabaseService {

	@Fluent
	DatabaseService getAccount(Integer id, Handler<AsyncResult<JsonObject>> resultHandler);
	
	@GenIgnore
	static DatabaseService create(JDBCClient jdbcClient,Handler<AsyncResult<DatabaseService>> readyHandler) {
		return new DatabaseServiceImpl(jdbcClient, readyHandler);
	}
	
	@GenIgnore
	static DatabaseService createProxy(Vertx vertx, String address) {
		return new DatabaseServiceVertxEBProxy(vertx, address);
	}
	
}
