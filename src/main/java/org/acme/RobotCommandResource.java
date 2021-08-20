package org.acme;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

// This is the main class where will control your robot from
@Path("/robotcommand")
public class RobotCommandResource {

    // Quarkus JMS takes care of creating a ConnectionFactory from the AMQ
    // Connection Settings that you have defined in the
    // src/main/resources/application.properties
    @Inject
    ConnectionFactory connectionFactory;

    // This is the name of your robot that you have to set in the
    // src/main/resources/application.properties
    @ConfigProperty(name = "robot.name")
    String robotName;

    Jsonb jsonb = JsonbBuilder.create();

    // This is RESTful endpoint that will be called to trigger your command. Add
    // your code here.
    @POST
    @Path("/run")
    @Produces(MediaType.TEXT_PLAIN)
    public String run() {

        // This is the AMQ context object that we will use to configure and communicate
        // with the AMQ broker
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {

            System.out.println("/run Enpoint called");

            // This producer object will take care of sending our messages to the queue
            System.out.println("Initializing producer");
            JMSProducer robotCommandMessageProducer = context.createProducer();

            // Here we create the command queue towards the robot
            System.out.println("Creating queue -> " + robotName + "Queue");
            Queue outgoingRobotCommandQueue = context.createQueue(robotName + "Queue");

            /*
             * For the advanced part of the hackathon we will need a Request/Reply pattern
             * to recieve response from our robot you can ignore it for the first part
             * 
             * // Create a temporary Response Queue for the robot to send the reply
             * Destination temporaryResponseQueue = context.createTemporaryQueue();
             * 
             * // Create a Consumer Object that will recieve the response message from the
             * robot 
             * JMSConsumer robotCommandMessageResponseConsumer =
             * context.createConsumer(temporaryResponseQueue);
             * 
             *
             */
            System.out.println("Sending Robot Command Messages");

            // Sample Request Commands (START HERE)
            
            //robotCommandMessageProducer.send(outgoingRobotCommandQueue, generateRequestRobotCommand(context, "forward", "1"));
            //robotCommandMessageProducer.send(outgoingRobotCommandQueue, generateRequestRobotCommand(context, "left", "90"));      
            
            /*        
            // Sample Request/Response Commands
            String correlationId = UUID.randomUUID().toString();
            robotCommandMessageProducer.send(outgoingRobotCommandQueue,
                    generateRequestReplyRobotCommand(context, "forward", "1"), robotCommandMessageResponseConsumer,
                    correlationId);

            TextMessage responseMessage = (TextMessage) robotCommandMessageResponseConsumer.receive(10000L);
            System.out.println(
                    "Response recieved from robot -> " + jsonb.fromJson(responseMessage.getText(), RobotCommand.class));
            System.out
                    .println("CorrelationID matched -> " + correlationId.equals(responseMessage.getJMSCorrelationID()));
            */


            System.out.println("Messages sent");

        } catch (Exception e) {
            System.out.println("An error occured while processing messages");
            e.printStackTrace();
        }
        return "OK";
    }

     // Create RequestRobotCommand Message
    private TextMessage generateRequestRobotCommand(JMSContext context, String command, String parameter)
            throws JMSException {

        RobotCommand robotCommand = new RobotCommand();
        robotCommand.setCommand(command);
        robotCommand.setParameter(parameter);

        TextMessage robotCommandMessage = context.createTextMessage();
        String robotCommandJson = jsonb.toJson(robotCommand);

        robotCommandMessage.setText(robotCommandJson);

        return robotCommandMessage;

    }

    // Create Request/Response RobotCommand Message
    private TextMessage generateRequestReplyRobotCommand(JMSContext context, String command, String parameter,
            Destination responseQueue, String correlationId) throws JMSException {

        RobotCommand robotCommand = new RobotCommand();
        robotCommand.setCommand(command);
        robotCommand.setParameter(parameter);

        String robotCommandJson = jsonb.toJson(robotCommand);

        TextMessage robotCommandMessage = (TextMessage) context.createTextMessage();

        robotCommandMessage.setJMSReplyTo(responseQueue);
        robotCommandMessage.setJMSCorrelationID(correlationId);

        robotCommandMessage.setText(robotCommandJson);

        return robotCommandMessage;

    }
}