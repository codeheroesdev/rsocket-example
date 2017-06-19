import io.reactivesocket.AbstractReactiveSocket;
import io.reactivesocket.ConnectionSetupPayload;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.client.ReactiveSocketClient;
import io.reactivesocket.lease.DisabledLeaseAcceptingSocket;
import io.reactivesocket.lease.LeaseEnforcingSocket;
import io.reactivesocket.server.ReactiveSocketServer;
import io.reactivesocket.server.ReactiveSocketServer.SocketAcceptor;
import io.reactivesocket.transport.TransportServer.StartedServer;
import io.reactivesocket.transport.tcp.client.TcpTransportClient;
import io.reactivesocket.transport.tcp.server.TcpTransportServer;
import io.reactivesocket.util.PayloadImpl;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;

import static io.reactivesocket.client.KeepAliveProvider.never;
import static io.reactivesocket.client.SetupProvider.keepAlive;

public final class StreamingClient {

    public static void main(String[] args) {
        //Start server which returns address on which it listens to
        SocketAddress address = startServer();

        //Start new client, connect it to address and return this clientSocket
        ReactiveSocket clientSocket = startClient(address);

        //Use clientSocket to make the request to server
        makeRequest(clientSocket);
    }

    private static void makeRequest(ReactiveSocket client) {
        //Let`s create the flow of responses from the server
        Flowable.fromPublisher(client.requestStream(new PayloadImpl("Can you count me something?")))
                //Extract data from a response
                .map(Payload::getData)
                //Convert data to String
                .map(StreamingClient::bufferToString)
                //Printout every received String
                .doOnNext(response -> System.out.println("Client received: " + response))
                //Consume only 10 responses
                .take(10)
                //Before closing stream, close client connection to the server
                .concatWith(Flowable.fromPublisher(client.close()).cast(String.class))
                //Wait for the last element from the stream
                .blockingLast();
    }


    private static SocketAddress startServer() {
        //Let`s start the server
        StartedServer server = ReactiveSocketServer
                //TCP transport will be used
                .create(TcpTransportServer.create())
                //Server needs a SocketAcceptor which is responsible for handling incoming connections
                .start(new ServerSocketAcceptor());

        //Returns server address which by default is randomized
        return server.getServerAddress();
    }

    private static ReactiveSocket startClient(SocketAddress address) {
        //Let`s start the client
        return Flowable.fromPublisher(ReactiveSocketClient.create(TcpTransportClient.create(address),
                //We are disabling keepAlive connection and leasing
                keepAlive(never()).disableLease())
                //Try to connect
                .connect())
                //Wait for a fresh ReactiveSocket to be used in the future
                .blockingFirst();
    }

    //Socket Acceptor used by server to accept new connections
    private static class ServerSocketAcceptor implements SocketAcceptor {
        @Override
        public LeaseEnforcingSocket accept(ConnectionSetupPayload setupPayload, ReactiveSocket reactiveSocket) {
            return new DisabledLeaseAcceptingSocket(new AbstractReactiveSocket() {
                @Override
                public Flux<Payload> requestStream(Payload payload) {
                    //Let`s print out every new request to server
                    System.out.println("Server received: " + bufferToString(payload.getData()));

                    //We will return Stream of responses
                    //Every 3 seconds we will respond with an incremented number
                    return Flux.interval(Duration.ofSeconds(3))
                            .map(aLong -> new PayloadImpl("Counting - " + aLong));

                }

            });
        }
    }

    /**
     * Helper function which converts ByteBuffer into string
     *
     * @param buffer ByteBuffer to be converted to string
     * @return String created from ByteBuffer
     */
    private static String bufferToString(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}