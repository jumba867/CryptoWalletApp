package com.example.ethereumwallet;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.web3j.tx.Transfer;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.crypto.WalletUtils;

import org.web3j.protocol.core.DefaultBlockParameterName;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String INFURA_URL = "https://sepolia.infura.io/v3/c9d85d78ed1e49639c0e12ce430e0a3b";
    private static final String WALLET_PASSWORD = "password"; // Password for your wallet
    private static final String WALLET_FILE_NAME = "my_wallet.json"; // Correct wallet file name

    private TextView tvBalance, tvPublicAddress, tvError; // Added tvError
    private Button btnViewBalance, btnSendEther, btnReceiveEther, btnCopyAddress;
    private EditText etRecipientAddress, etAmount;
    private ExecutorService executorService;
    private Web3j web3j;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add BouncyCastle provider
        Security.addProvider(new BouncyCastleProvider());

        // Initialize views
        tvBalance = findViewById(R.id.tvBalance);
        tvPublicAddress = findViewById(R.id.tvPublicAddress);
        tvError = findViewById(R.id.tvError); // Initialize error TextView
        btnViewBalance = findViewById(R.id.btnViewBalance);
        btnSendEther = findViewById(R.id.btnSendEther);
        btnReceiveEther = findViewById(R.id.btnReceiveEther);
        btnCopyAddress = findViewById(R.id.btnCopyAddress); // Copy Address Button
        etRecipientAddress = findViewById(R.id.etRecipientAddress);
        etAmount = findViewById(R.id.etAmount);
        progressBar = findViewById(R.id.progressBar);

        // Executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Web3j setup
        web3j = Web3j.build(new HttpService(INFURA_URL));

        // View Balance button
        btnViewBalance.setOnClickListener(v -> viewBalance());

        // Send Ether button
        btnSendEther.setOnClickListener(v -> {
            String recipient = etRecipientAddress.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            if (!recipient.isEmpty() && !amount.isEmpty() && isValidAmount(amount)) {
                sendEther(recipient, new BigDecimal(amount));
            } else {
                showError("Please enter valid recipient address and amount.");
            }
        });

        // Receive Ether button
        btnReceiveEther.setOnClickListener(v -> showPublicAddress());

        // Copy Address button
        btnCopyAddress.setOnClickListener(v -> copyPublicAddress());
    }

    private void viewBalance() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setText(""); // Clear previous errors
        executorService.submit(() -> {
            try {
                File walletFile = new File(getFilesDir(), WALLET_FILE_NAME);
                if (!walletFile.exists()) {
                    Log.e("Wallet Error", "Wallet file not found");
                    runOnUiThread(() -> {
                        showError("Wallet file not found. Please create a wallet.");
                        progressBar.setVisibility(View.GONE);
                    });
                    return;
                }

                Credentials credentials = WalletUtils.loadCredentials(WALLET_PASSWORD, walletFile);
                EthGetBalance balance = web3j.ethGetBalance(
                        credentials.getAddress(),
                        DefaultBlockParameterName.LATEST
                ).send();
                BigDecimal ethBalance = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER);

                runOnUiThread(() -> {
                    tvBalance.setText(ethBalance.toPlainString() + " ETH");
                    progressBar.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                Log.e("Error", e.getMessage(), e);
                runOnUiThread(() -> {
                    showError("Error fetching balance: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void sendEther(String recipient, BigDecimal amount) {
        if (!isValidEthereumAddress(recipient)) {
            showError("Invalid Ethereum address.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvError.setText(""); // Clear previous errors
        executorService.submit(() -> {
            try {
                File walletFile = new File(getFilesDir(), WALLET_FILE_NAME);
                if (!walletFile.exists()) {
                    runOnUiThread(() -> {
                        showError("Wallet file not found.");
                        progressBar.setVisibility(View.GONE);
                    });
                    return;
                }

                Credentials credentials = WalletUtils.loadCredentials(WALLET_PASSWORD, walletFile);
                EthGetBalance balance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
                BigDecimal walletBalance = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER);

                if (walletBalance.compareTo(amount) < 0) {
                    runOnUiThread(() -> {
                        showError("Insufficient balance.");
                        progressBar.setVisibility(View.GONE);
                    });
                    return;
                }

                TransactionReceipt receipt = Transfer.sendFunds(
                        web3j, credentials, recipient, amount, Convert.Unit.ETHER
                ).send();

                runOnUiThread(() -> {
                    tvBalance.setText("Transaction successful: " + receipt.getTransactionHash());
                    progressBar.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                Log.e("Transaction Error", e.getMessage(), e);
                runOnUiThread(() -> {
                    showError("Transaction failed: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private boolean isValidEthereumAddress(String address) {
        if (address == null || !address.startsWith("0x") || address.length() != 42) {
            return false;
        }
        return address.matches("^(0x)?[0-9a-fA-F]{40}$");
    }

    private boolean isValidAmount(String amount) {
        try {
            new BigDecimal(amount); // Try to convert the string to BigDecimal
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
     //Change 1
    private void showPublicAddress() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setText(""); // Clear previous errors
        executorService.submit(() -> {
            try {
                File walletFile = new File(getFilesDir(), WALLET_FILE_NAME);
                if (!walletFile.exists()) {
                    runOnUiThread(() -> {
                        showError("Wallet file not found.");
                        progressBar.setVisibility(View.GONE);
                    });
                    return;
                }

                Credentials credentials = WalletUtils.loadCredentials(WALLET_PASSWORD, walletFile);
                String publicAddress = credentials.getAddress();

                runOnUiThread(() -> {
                    tvPublicAddress.setText(publicAddress);
                    progressBar.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                Log.e("Wallet Error", e.getMessage(), e);
                runOnUiThread(() -> {
                    showError("Error loading wallet: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
    }

    // Method to copy the public address to clipboard
    private void copyPublicAddress() {
        String publicAddress = tvPublicAddress.getText().toString().replace("Public Address: ", "");
        if (!publicAddress.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Ethereum Address", publicAddress);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Address copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            showError("No public address available to copy.");
        }
    }
}
