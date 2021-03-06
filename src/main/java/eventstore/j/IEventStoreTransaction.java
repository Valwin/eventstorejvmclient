package eventstore.j;

import eventstore.EventData;
import scala.Unit;
import scala.concurrent.Future;

import java.util.Collection;

/**
 * @author Yaroslav Klymko
 */
public interface IEventStoreTransaction {
    public Long getId();

    public Future<Unit> write(Collection<EventData> events);

    public Future<Unit> commit();

    public void rollback();
}