
package com.comp713;

import com.comp713.grpc.TaskEventServiceGrpc;
import com.comp713.grpc.TaskStatusChangeRequest;
import com.comp713.grpc.TaskStatusChangeResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class TaskEventGrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder
                .forPort(9090)
                .addService(new TaskEventService())
                .build()
                .start();

        System.out.println("gRPC Task Event Service running on port 9090");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping gRPC service...");
            server.shutdown();
        }));

        server.awaitTermination();
    }

    static class TaskEventService extends TaskEventServiceGrpc.TaskEventServiceImplBase {

        @Override
        public void recordTaskStatusChange(
                TaskStatusChangeRequest request,
                StreamObserver<TaskStatusChangeResponse> responseObserver) {

            System.out.println("Task status change received:");
            System.out.println("Task ID: " + request.getTaskId());
            System.out.println("Task title: " + request.getTaskTitle());
            System.out.println("Old status: " + request.getOldStatus());
            System.out.println("New status: " + request.getNewStatus());
            System.out.println("User ID: " + request.getUserId());

            TaskStatusChangeResponse response = TaskStatusChangeResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Task status change recorded")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
