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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    private RecetaAdapter recipeAdapter;
    private List<Receta> recipeList;
    private DatabaseHelper databaseHelper;
    private String idioma = "es";
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

        builder.setView(dialogView)
                .setTitle("Agregar Receta")
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String recipeName = recipeNameEditText.getText().toString().trim();
                        String recipeIngredientes = recipeIngredientesText.getText().toString().trim();

                        if (!recipeName.isEmpty() && !recipeIngredientes.isEmpty()) {
                            long recId = databaseHelper.addRecipe(recipeName, recipeIngredientes);
                            Receta receta = new Receta(recId, recipeName, recipeIngredientes);
                            receta.setId((int) recId);
                            recipeList.add(receta);
                            recipeAdapter.notifyDataSetChanged();
                            showNotification();
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

        builder.create().show();
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




