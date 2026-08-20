package com.marlabs.pccredito.idempotency;

import org.springframework.stereotype.Service;

/**
 * Defines the lifecycle needed to prevent duplicate NovaProposta operations.
 *
 * <p>TODO: replace this scaffold with a durable, shared reservation store before
 * enabling NovaProposta in production. In-memory storage is intentionally not used
 * because it would not protect multiple instances or process restarts.</p>
 */
@Service
public class IdempotencyService {

	public Reservation reserve(String idempotencyKey, String requestId) {
		// TODO: atomically reserve the key in durable storage before any retryable operation.
		return new Reservation(idempotencyKey, requestId);
	}

	public void complete(Reservation reservation) {
		// TODO: persist the terminal technical result for deterministic replay.
	}

	public void fail(Reservation reservation) {
		// TODO: define the formal recovery policy before releasing or retaining a reservation.
	}

	public record Reservation(String idempotencyKey, String requestId) {
	}
}

