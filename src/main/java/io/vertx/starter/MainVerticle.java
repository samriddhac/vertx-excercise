package io.vertx.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

	/**
	 * There are 2 forms of start (and stop) methods: 1 with no argument and 
	 * 1 with a future object reference. The no-argument variants imply that the verticle initialization 
	 * or house-keeping phases always succeed unless an exception is being thrown. 
	 * The variants with a future object provide a more fine-grained approach to eventually signal 
	 * that operations succeeded or not. Indeed, some initialization or cleanup code may require asynchronous operations, 
	 * so reporting via a future object naturally fits with asynchronous idioms.
	 * 
	 * Vert.x futures are not JDK futures: they can be composed and queried in a non-blocking fashion. 
	 * They shall be used for simple coordination of asynchronous tasks, and especially those of deploying verticles and 
	 * checking if they were successfully deployed or not.
	 */
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Future<String> dbVerticleDeployment = Future.future();
		vertx.deployVerticle(new DatabaseVerticle(), dbVerticleDeployment.completer());
		
		dbVerticleDeployment.compose((id)->{
			Future<String> httpVerticleDeployment = Future.future();
			vertx.deployVerticle("io.vertx.starter.HttpServerVerticle", new DeploymentOptions().setInstances(2),
					httpVerticleDeployment.completer());
			return httpVerticleDeployment;
		}).setHandler((ar) -> {
			/**
			 * ar is of type AsyncResult<Void>. AsyncResult<T> is used to pass the result of an 
			 * asynchronous processing and may either yield a value of type T on success 
			 * or a failure exception if the processing failed.
			 */
			if(ar.succeeded()) {
				LOGGER.info("Verticles deployed");
				startFuture.complete();
			}
			else {
				startFuture.fail(ar.cause());
			}
		});
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}
	
}
