package actors;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorIdentity;
import akka.actor.Identify;
import akka.actor.Terminated;
import akka.actor.AbstractActor;
import akka.actor.ReceiveTimeout;

import javax.inject.Inject;
import java.util.Optional;

public class LookupActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(LookupActor.class);

    private final String path;
    private ActorRef calculator = null;

    @Inject
    public LookupActor(Config config) {
        this(config.getString("lookup.path"));
    }

    public LookupActor(String path) {
        this.path = path;
        sendIdentifyRequest();
    }

    private void sendIdentifyRequest() {
        getContext().actorSelection(path).tell(new Identify(path), self());
        getContext()
                .system()
                .scheduler()
                .scheduleOnce(Duration.create(3, SECONDS), self(),
                        ReceiveTimeout.getInstance(), getContext().dispatcher(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorIdentity.class, identity -> {
                    Optional<ActorRef> calculatorRef = identity.getActorRef();
                    if (calculatorRef.isPresent()) {
                        calculator = calculatorRef.get();
                        getContext().watch(calculator);
                        getContext().become(active, true);
                    } else {
                        logger.info("Remote actor not available: " + path);
                    }
                })
                .match(ReceiveTimeout.class, x -> {
                    sendIdentifyRequest();
                })
                .build();
    }

    Receive active = receiveBuilder()
            .match(Op.MathOp.class, message -> {
                // send message to server actor
                calculator.tell(message, self());
            })
            .match(Op.AddResult.class, result -> {
                logger.info("Add result: {} + {} = {}", result.getN1(), result.getN2(), result.getResult());
            })
            .match(Op.SubtractResult.class, result -> {
                logger.info("Sub result: {} - {} = {}", result.getN1(), result.getN2(), result.getResult());
            })
            .match(Terminated.class, terminated -> {
                logger.info("Calculator terminated");
                sendIdentifyRequest();
                getContext().unbecome();
            })
            .match(ReceiveTimeout.class, message -> {
                // ignore
            })
            .build();

}
