package com.comp713;

import com.comp713.grpc.TaskEventServiceGrpc;
import com.comp713.grpc.TaskStatusChangeRequest;
import com.comp713.grpc.TaskStatusChangeResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class TaskEventGrpcClient {

    public static TaskStatusChangeResponse recordTaskStatusChange(
            int taskId,
            String taskTitle,
            String oldStatus,
            String newStatus,
            int userId) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        try {
            TaskEventServiceGrpc.TaskEventServiceBlockingStub stub =
                    TaskEventServiceGrpc.newBlockingStub(channel)
                            .withDeadlineAfter(3, TimeUnit.SECONDS);

            TaskStatusChangeRequest request = TaskStatusChangeRequest.newBuilder()
                    .setTaskId(taskId)
                    .setTaskTitle(taskTitle)
                    .setOldStatus(oldStatus)
                    .setNewStatus(newStatus)
                    .setUserId(userId)
                    .build();

            return stub.recordTaskStatusChange(request);

        } finally {
            channel.shutdown();

            try {
                if (!channel.awaitTermination(1, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
