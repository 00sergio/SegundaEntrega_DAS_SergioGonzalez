package com.example.cookit;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    private RecetaAdapter recipeAdapter;
    private List<Receta> recipeList;
    private DatabaseHelper databaseHelper;
    private String idioma = "es";
    private String foto_base64;
    private long id;
    private static final String CHANNEL_ID = "RecetaChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idioma = extras.getString("IDIOMA");
            Locale nuevaloc = new Locale(idioma);
            Locale.setDefault(nuevaloc);
            Configuration configuration = getBaseContext().getResources().getConfiguration();
            configuration.setLocale(nuevaloc);
            configuration.setLayoutDirection(nuevaloc);
            Context context = getBaseContext().createConfigurationContext(configuration);
            getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        }
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        recipeList = databaseHelper.getAllRecipes();

        recyclerViewRecipes = findViewById(R.id.recyclerView_recipes);
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));

        recipeAdapter = new RecetaAdapter(recipeList);
        recyclerViewRecipes.setAdapter(recipeAdapter);

        Button addButton = findViewById(R.id.button_addRecipe);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRecipeDialog();
            }
        });

        recipeAdapter.setOnItemClickListener(new RecetaAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                showEditRecetaDialog(position);

            }

            @Override
            public void onDeleteClick(int position) {
                showDeleteRecetaDialog(position);
            }
        });

        Button cambiarIdioma_eus = findViewById(R.id.btn_eus);
        cambiarIdioma_eus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("eu");

            }
        });

        Button cambiarIdioma_en = findViewById(R.id.btn_en);
        cambiarIdioma_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("en");
            }
        });

        Button cambiarIdioma_es = findViewById(R.id.btn_es);
        cambiarIdioma_es.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("es");
            }
        });
    }

    private void changeLanguage(String languageCode) {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("IDIOMA", languageCode);
        finish();
        startActivity(intent);
        String language;
        if (languageCode.equals("es")) {
            language = "español";
        } else if (languageCode.equals("en")) {
            language = "inglés";
        } else {
            language = "euskera";
        }
        Toast.makeText(getApplicationContext(), "Cambiar a " + language, Toast.LENGTH_LONG).show();
    }

    private void showAddRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_anadir_receta, null);
        builder.setView(dialogView);
        final EditText recipeNameEditText = dialogView.findViewById(R.id.recipeNameEditText);
        final EditText recipeIngredientesText = dialogView.findViewById(R.id.recipeIngredientesEditText);
        Button buttonCaptureImage = dialogView.findViewById(R.id.fotoboton);

        builder.setView(dialogView)
                .setTitle("Agregar Receta")
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String recipeName = recipeNameEditText.getText().toString().trim();
                        String recipeIngredientes = recipeIngredientesText.getText().toString().trim();

                        if (!recipeName.isEmpty() && !recipeIngredientes.isEmpty()) {
                            long recId = databaseHelper.addRecipe(recipeName, recipeIngredientes);
                            Receta receta = new Receta(recId, recipeName, recipeIngredientes);
                            receta.setId((int) recId);
                            recipeList.add(receta);
                            recipeAdapter.notifyDataSetChanged();
                            showNotification();
                            guardarRecetaFoto(recId, foto_base64);
                            cargarFoto(String.valueOf(recId));
                        } else {
                            Toast.makeText(MainActivity.this, "Ingrese el nombre y los ingredientes de la receta", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        buttonCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //lanzar foto con camara
                Intent elIntentFoto= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureLauncher.launch(elIntentFoto);
            }
        });

        builder.create().show();
    }

    private void cargarFoto(String identificador){
        // URL del script para recuperar la imagen
        String url = "http://34.66.46.125:81/cargar_imagen.php";
        String parametro = "recId=" + Uri.encode(identificador);

        // Ejecutar la tarea asíncrona para recuperar la imagen
        new ObtenerImagenTask().execute(url, parametro);
    }

    private void mostrarImagen(Bitmap imagen) {
        ImageView imageView = findViewById(R.id.recipe_image);
        imageView.setImageBitmap(imagen);
    }

    private class ObtenerImagenTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            String urlString = params[0];
            String parametro = params[1];
            Bitmap imagen = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setDoOutput(true);

                // Escribir los parámetros en la solicitud
                OutputStream outputStream = conexion.getOutputStream();
                outputStream.write(parametro.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                // Establecer la conexión HTTP y recibir la respuesta del servidor
                int responseCode = conexion.getResponseCode();

                // Verificar si la conexión fue exitosa (código de respuesta HTTP 200)
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta del servidor
                    InputStream inputStream = conexion.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    // Leer línea por línea y construir el StringBuilder
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    // Cerrar el BufferedReader y el InputStream
                    bufferedReader.close();
                    inputStream.close();

                    // Obtener el string de la respuesta del servidor
                    String base64String = stringBuilder.toString();

                    // Decodificar la cadena base64 en un array de bytes
                    byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

                    // Crear un Bitmap a partir de los bytes decodificados
                    imagen = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                } else {
                    // Si la conexión no fue exitosa, manejar el error adecuadamente
                    Log.d("Error", "Imagen no recibida");
                }

                conexion.disconnect();
            } catch (Exception e) {
                Log.e("Error", "Error al recuperar : " + e.getMessage());
            }

            return imagen;
        }

        @Override
        protected void onPostExecute(Bitmap imagen) {
            if (imagen != null) {
                mostrarImagen(imagen);
            } else {
                // Si no se pudo obtener la imagen, mostrar una imagen por defecto o manejar el error
                Log.e("Error", "No se pudo obtener la imagen");
            }
        }
    }

    private ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK &&
                        result.getData()!= null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap laminiatura = (Bitmap) bundle.get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    laminiatura.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] fototransformada = stream.toByteArray();
                    foto_base64 = Base64.encodeToString(fototransformada,Base64.DEFAULT);
                } else {
                    Log.d("Foto", "No foto");
                }
            });

    private void guardarRecetaFoto(Long id, String foto) {
        // URL de tu archivo PHP en el servidor
        String url = "http://34.66.46.125:81/guardar_foto.php";

        // Dentro del método enviarDatos()
        String parametros = "recId=" + Uri.encode(String.valueOf(id)) + "&foto_base64=" + Uri.encode((foto));
        Log.d("recibido", parametros); // Imprime los datos enviados en el registro (Logcat)

        // Ejecutar la tarea asíncrona
        new EnviarDatosTask().execute(url, parametros);
    }

    private static class EnviarDatosTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Obtener la URL y los parámetros de los argumentos
            String urlString = params[0];
            String parametros = params[1];

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

                // Obtener la respuesta del servidor (si es necesario)
                int responseCode = conexion.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("Bien", "Bien");
                } else {
                    Log.d("Mal", "Algo no ok");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void showEditRecetaDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_anadir_receta, null);
        builder.setView(dialogView);

        final EditText name = dialogView.findViewById(R.id.recipeNameEditText);
        final EditText ing = dialogView.findViewById(R.id.recipeIngredientesEditText);

        Receta receta = recipeList.get(position);
        name.setText(receta.getNombre());
        ing.setText(receta.getIngredientes());

        builder.setView(dialogView)
                .setTitle("Editar Receta")
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String recipeName = name.getText().toString().trim();
                        String recipeIngredientes = ing.getText().toString().trim();

                        if (!recipeName.isEmpty() && !recipeIngredientes.isEmpty()) {
                            Receta newReceta = recipeList.get(position);
                            newReceta.setNombre(recipeName);
                            newReceta.setIngredientes(recipeIngredientes);
                            databaseHelper.updateRecipe(newReceta);
                            recipeAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(MainActivity.this, "Ingrese el nombre de la receta", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    private void showDeleteRecetaDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Tarea");
        builder.setMessage("¿Estas seguro?");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                borrarReceta(position);

            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void borrarReceta(int position) {
        Receta receta = recipeList.get(position);
        databaseHelper.deleteRecipe(receta.getId());
        recipeList.remove(position);
        recipeAdapter.notifyItemRemoved(position);
        Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show();
    }

    private void showNotification() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Nueva Receta Agregada")
                    .setContentText("Se ha agregado una nueva receta a la lista.")
                    .setVibrate(new long[]{0, 1000, 500, 1000})
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Recetas", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0, builder.build());
        }
    }
}
