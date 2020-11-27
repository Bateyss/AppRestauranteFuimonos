package com.fm.modules.app.carrito;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.app.menu.MenuBotton;
//import com.fm.modules.app.carrito.RestaurantePorCategoria;

public class PedidoNoRegistrado extends AppCompatActivity {

    private Button inicioBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_no_registrado);
        inicioBtn = (Button) findViewById(R.id.orderToInicio2);
        inicioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PedidoNoRegistrado.this, MenuBotton.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        onBack();
    }

    public void onBack() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Intent intent = new Intent(PedidoNoRegistrado.this, MenuBotton.class);
                startActivity(intent);
            }
        };
        getOnBackPressedDispatcher().addCallback(PedidoNoRegistrado.this, callback);
    }
}