package io.vertx.starter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class HttpServerVerticle extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

	public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
	public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";
	
	private String wikiDbQueue = "wikidb.queue";
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		/**
		 * The AbstractVerticle#config() method allows accessing the verticle configuration 
		 * that has been provided. The second parameter is a default value in case no specific value was given.
		 * Configuration values can not just be String objects but also integers, boolean values, complex JSON data, etc.
		 */
		wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue");
		
		HttpServer httpServer = vertx.createHttpServer();
		Router router = Router.router(vertx);
		
		router.get("/accoount-service/accounts/:id").handler(this::getAccounts);
		
		httpServer
		.requestHandler(router)
		.listen(8080, (ar) -> {
			if(ar.succeeded()) {
				LOGGER.info("HTTP server running on port 8080");
				startFuture.complete();
			}
			else {
				LOGGER.error("Could not start a HTTP server", ar.cause());
				startFuture.fail(ar.cause());
			}
		});
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		// TODO Auto-generated method stub
		super.stop(stopFuture);
	}
	
	private void getAccounts(RoutingContext routingContext) {
		int id = Integer.valueOf(routingContext.request().getParam("id"));
		
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "get-account");
		vertx.eventBus().send(wikiDbQueue, new JsonObject().put("id", id), deliveryOptions, (response) -> {
			if (response.succeeded()) {
				JsonObject payload = (JsonObject) response.result().body();
				routingContext.response().setStatusCode(200);
		        routingContext.response().putHeader("Content-Type", "application/json");
		        routingContext.response().end(payload.encode());
			}
			else {
				routingContext.response().setStatusCode(500);
			}
		});
	}

	
}
