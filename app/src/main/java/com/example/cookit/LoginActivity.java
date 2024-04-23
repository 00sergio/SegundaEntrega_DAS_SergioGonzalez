package com.example.cookit;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.registerButton);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });

        // Corregir el ID del botón de registro en LoginActivity
        Button buttonRegistro = findViewById(R.id.registerButton);

        buttonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir a la actividad de registro
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void iniciarSesion() {
        // Obtener referencias a los campos de usuario y contraseña
        EditText editTextUsuario = findViewById(R.id.editTextUsername);
        EditText editTextContrasena = findViewById(R.id.editTextPassword);

        // Obtener los valores de usuario y contraseña
        String usuario = editTextUsuario.getText().toString();
        String contrasena = editTextContrasena.getText().toString();

        // Enviar solicitud al servidor para verificar la autenticación
        String url = "http://34.66.46.125:81/login.php"; // URL del script para verificar la autenticación
        String parametros = "usuario=" + Uri.encode(usuario) + "&contrasena=" + Uri.encode(contrasena);

        // Ejecutar la tarea asíncrona para enviar la solicitud
        new VerificarLoginTask().execute(url, parametros);
    }

    private class VerificarLoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String urlString = params[0];
            String parametros = params[1];
            boolean autenticado = false;

            try {
                // Crear la conexión HTTP
                URL url = new URL(urlString);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setDoOutput(true);

                // Escribir los parámetros en la solicitud
                OutputStream outputStream = conexion.getOutputStream();
                outputStream.write(parametros.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                // Leer la respuesta del servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Autenticado")) {
                        autenticado = true;
                    }
                }
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return autenticado;
        }

        @Override
        protected void onPostExecute(Boolean autenticado) {
            if (autenticado) {
                // Si el usuario está autenticado, mostrar mensaje de éxito y abrir MainActivity
                Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                // Si no está autenticado, mostrar un mensaje de error
                Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
