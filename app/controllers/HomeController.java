package controllers;

import actors.Op;
import akka.actor.ActorRef;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final ActorRef lookupActor;

    @Inject
    public HomeController(@Named("lookup-actor") ActorRef lookupActor) {
        this.lookupActor = lookupActor;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    public Result add(int a, int b) {
        // Fire-and-forget
        lookupActor.tell(new Op.Add(a, b), null);
        return ok("Add: See the logs for result");
    }

    public Result subtract(int a, int b) {
        // Fire-and-forget
        lookupActor.tell(new Op.Subtract(a, b), null);
        return ok("Subtract: See the logs for result");
    }
}
