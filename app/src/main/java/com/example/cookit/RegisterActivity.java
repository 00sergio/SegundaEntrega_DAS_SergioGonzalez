package com.example.cookit;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Botón de registro
        Button buttonRegistro = findViewById(R.id.registerButton);

        // Configurar el click listener del botón
        buttonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        // Obtener referencias a los campos de usuario y contraseña
        EditText editTextUsuario = findViewById(R.id.registerEditTextUsername);
        EditText editTextContrasena = findViewById(R.id.registerEditTextPassword);

        // Obtener los valores de usuario y contraseña
        String usuario = editTextUsuario.getText().toString();
        String contrasena = editTextContrasena.getText().toString();

        // Enviar solicitud al servidor para registrar el usuario
        String url = "http://34.66.46.125:81/registro.php"; // URL del script para procesar el registro
        String parametros = "usuario=" + Uri.encode(usuario) + "&contrasena=" + Uri.encode(contrasena);

        // Ejecutar la tarea asíncrona para enviar la solicitud
        new RegistrarUsuarioTask().execute(url, parametros);
    }

    // Clase para registrar usuario en segundo plano
    private class RegistrarUsuarioTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // Obtener la URL y los parámetros de los argumentos
            String urlString = params[0];
            String parametros = params[1];
            String respuesta = "";

            try {
                // Crear la conexión HTTP
                URL url = new URL(urlString);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                conexion.setRequestMethod("POST");
                conexion.setDoOutput(true);

                // Escribir los parámetros en la solicitud
                OutputStream outputStream = conexion.getOutputStream();
                outputStream.write(parametros.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                // Leer la respuesta del servidor
                int responseCode = conexion.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // La solicitud fue exitosa
                    // Leer la respuesta del servidor
                    respuesta = "Usuario registrado exitosamente";
                } else {
                    // La solicitud falló
                    respuesta = "Error al registrar el usuario: " + conexion.getResponseMessage();
                }

                // Cerrar la conexión
                conexion.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                respuesta = "Error al registrar el usuario: " + e.getMessage();
            }

            return respuesta;
        }

        // Después de registrar el usuario exitosamente
        @Override
        protected void onPostExecute(String respuesta) {
            // Mostrar el mensaje de confirmación
            Toast.makeText(RegisterActivity.this, respuesta, Toast.LENGTH_SHORT).show();

            // Si el registro fue exitoso, volver a la página de inicio de sesión
            if (respuesta.equals("Usuario registrado exitosamente")) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Finalizar la actividad actual para que el usuario no pueda volver atrás
            }
        }

    }
}
