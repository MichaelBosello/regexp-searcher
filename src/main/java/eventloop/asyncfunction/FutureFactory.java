package eventloop.asyncfunction;

import io.vertx.core.Future;

public interface FutureFactory<T> {
    Future<T> compose();
}
