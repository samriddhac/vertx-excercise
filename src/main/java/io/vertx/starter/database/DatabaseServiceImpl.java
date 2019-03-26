package io.vertx.starter.database;

import java.util.Optional;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class DatabaseServiceImpl implements DatabaseService {

	JDBCClient jdbcClient;
	
	public DatabaseServiceImpl(JDBCClient jdbcClient, Handler<AsyncResult<DatabaseService>> readyHandler) {
		this.jdbcClient = jdbcClient;
		readyHandler.handle(Future.succeededFuture(this));
	}
	
	@Override
	public DatabaseService getAccount(Integer id, Handler<AsyncResult<JsonObject>> resultHandler) {
		jdbcClient.queryWithParams(IQuery.ACCOUNT.FIND_BY_ACCID, new JsonArray().add(id), response -> {
			if (response.succeeded()) {
				Optional<JsonArray> row = response.result().getResults()
						.stream()
						.findFirst();
				if(row.isPresent()) {
					JsonArray rowItem = (JsonArray) row.get();
					JsonObject payload = new JsonObject()
							.put("id", rowItem.getInteger(0))
							.put("accountNumber", rowItem.getString(1))
							.put("allowedScheduledTransaction", rowItem.getString(2));
					resultHandler.handle(Future.succeededFuture(payload));
				}
			}
			else {
				resultHandler.handle(Future.failedFuture(response.cause()));
			}
		});
		return this;
	}

}
