
package com.comp713;

import com.comp713.grpc.TaskEventServiceGrpc;
import com.comp713.grpc.TaskStatusChangeRequest;
import com.comp713.grpc.TaskStatusChangeResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class TaskEventGrpcServer {

    public static void main(String[] args)
            throws IOException, InterruptedException {

        // Create the database table before accepting events.
        TaskEventDAO.createTable();

        Server server = ServerBuilder
                .forPort(9090)
                .addService(new TaskEventService())
                .build()
                .start();

        System.out.println(
                "gRPC Task Event Service running on port 9090"
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping gRPC service...");
            server.shutdown();
        }));

        server.awaitTermination();
    }

    static class TaskEventService
            extends TaskEventServiceGrpc.TaskEventServiceImplBase {

        @Override
        public void recordTaskStatusChange(
                TaskStatusChangeRequest request,
                StreamObserver<TaskStatusChangeResponse> responseObserver) {
                System.out.println("Received gRPC request for task ID: " + request.getTaskId());


            boolean saved = TaskEventDAO.saveEvent(
                    request.getTaskId(),
                    request.getTaskTitle(),
                    request.getOldStatus(),
                    request.getNewStatus(),
                    request.getUserId()
            );

            TaskStatusChangeResponse response =
                    TaskStatusChangeResponse.newBuilder()
                            .setSuccess(saved)
                            .setMessage(
                                    saved
                                            ? "Task status event saved"
                                            : "Failed to save task status event"
                            )
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            if (saved) {
                System.out.println(
                        "Task status event saved for task ID: "
                        + request.getTaskId()
                );
            }
        }
    }
}
