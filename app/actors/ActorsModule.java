package actors;

import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

public class ActorsModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {
        // bindActor will bind LookupActor as an eager singleton. That is the
        // default behavior for that method.
        bindActor(LookupActor.class, "lookup-actor");
    }
}
