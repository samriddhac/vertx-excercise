package io.vertx.starter.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ServiceBinder;

public class DatabaseVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseVerticle.class);

	private JDBCClient dbClient;

	public static final String CONFIG_WIKIDB_JDBC_URL = "wikidb.jdbc.url";
	public static final String CONFIG_WIKIDB_JDBC_DRIVER_CLASS = "wikidb.jdbc.driver_class";
	public static final String CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE = "wikidb.jdbc.max_pool_size";

	public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

	private String wikiDbQueue = "wikidb.queue";

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOGGER.info("Database verticle deployment starts");
		/**
		 * createShared creates a shared connection to be shared among verticles known to the vertx instance.
		 * JDBCClient.createNonShared(vertx, config);--> To create non shared data source
		 */
		dbClient = JDBCClient.createShared(vertx, new JsonObject()
				.put("url", "jdbc:mysql://localhost:3306/demo?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true")
				.put("driver_class", "com.mysql.jdbc.Driver")
				.put("user", "root")
				.put("password", "root")
				.put("max_pool_size", 30));
		/**
		 * Registering a service requires an interface class, a Vert.x context, an implementation and an event bus destination.
		 * The WikiDatabaseServiceVertxEBProxy generated class handles receiving messages on the event bus and then 
		 * dispatching them to the WikiDatabaseServiceImpl
		 */
		DatabaseService.create(dbClient, (ready) -> {
			LOGGER.info("DatabaseService create starts");
			if(ready.succeeded()) {
				/**
				 * To directly send via event bus.
				 * vertx.eventBus().consumer(config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue"), this::onMessage);
				 */
				
				/**
				 * Registering a service requires an interface class, a Vert.x context, an implementation and an event bus destination.
				 * The WikiDatabaseServiceVertxEBProxy generated class handles receiving messages on the event bus and then 
				 * dispatching them to the WikiDatabaseServiceImpl
				 */
				ServiceBinder binder = new ServiceBinder(vertx);
				binder.setAddress(CONFIG_WIKIDB_QUEUE)
				.register(DatabaseService.class, ready.result());
				startFuture.complete();
				LOGGER.info("Database verticle deployed");
			}
			else {
				LOGGER.error("Database error", ready.cause());
				startFuture.fail(ready.cause());
			}
		});
	}
	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		// TODO Auto-generated method stub
		super.stop(stopFuture);
	}

	/*public void onMessage(Message<JsonObject> message) {
		if (!message.headers().contains("action")) {
			LOGGER.error("No action header specified for message with headers {} and body {}",
					message.headers(), message.body().encodePrettily());
			message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
			return;
		}
		String action = message.headers().get("action");
		switch (action) {
		case "get-account":
			getAccounts(message);
			break;
		default:
			message.fail(ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);
		}

	}

	private void reportQueryError(Message<JsonObject> message, Throwable cause) {
		LOGGER.error("Database query error", cause);
		message.fail(ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
	}

	public enum ErrorCodes {
		NO_ACTION_SPECIFIED,
		BAD_ACTION,
		DB_ERROR
	}*/
}