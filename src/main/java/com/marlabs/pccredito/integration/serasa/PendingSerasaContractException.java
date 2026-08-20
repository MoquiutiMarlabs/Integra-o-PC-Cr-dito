package com.marlabs.pccredito.integration.serasa;

public class PendingSerasaContractException extends RuntimeException {

	public static final String CONTRACT_ID = "SERASA-CONTRACT-001";

	public PendingSerasaContractException() {
		super(CONTRACT_ID + ": Serasa PJ input ownership and requiredness are pending confirmation");
	}
}

