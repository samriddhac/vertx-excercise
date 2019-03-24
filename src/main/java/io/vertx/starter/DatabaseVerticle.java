package io.vertx.starter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

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
		dbClient.getConnection((ar) -> {
			if(ar.failed()) {
				LOGGER.error("Could not open a database connection", ar.cause());
				startFuture.fail(ar.cause());
			}
			else {
				vertx.eventBus().consumer(config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue"), this::onMessage);
				startFuture.complete();
			}
		});
	}
	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		// TODO Auto-generated method stub
		super.stop(stopFuture);
	}

	public void onMessage(Message<JsonObject> message) {
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

	private void getAccounts(Message<JsonObject> message) {
		Integer id = message.body().getInteger("id");
		dbClient.queryWithParams(IQuery.ACCOUNT.FIND_BY_ACCID, new JsonArray().add(id), response -> {
			if (response.succeeded()) {
				Optional<JsonArray> row = response.result().getResults()
						.stream()
						.findFirst();
				if(row.isPresent()) {
					JsonArray rowItem = (JsonArray) row.get();
					LOGGER.info("rowItem "+ rowItem.toString());
					JsonObject payload = new JsonObject()
							.put("id", rowItem.getInteger(0))
							.put("accountNumber", rowItem.getString(1))
							.put("allowedScheduledTransaction", rowItem.getString(2));
					message.reply(payload);
				}
			}
			else {
				reportQueryError(message, response.cause());
			}
		});
	}

	private void reportQueryError(Message<JsonObject> message, Throwable cause) {
		LOGGER.error("Database query error", cause);
		message.fail(ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
	}

	public enum ErrorCodes {
		NO_ACTION_SPECIFIED,
		BAD_ACTION,
		DB_ERROR
	}
}