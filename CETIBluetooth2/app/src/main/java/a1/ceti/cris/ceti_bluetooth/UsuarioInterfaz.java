package a1.ceti.cris.ceti_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class UsuarioInterfaz extends AppCompatActivity implements View.OnClickListener {
    /*DECLARACIÓN DE LOS ELEMENTOS------------------------------------------------------------------>|*/
    Button btnIzquierda, btnDerecha,btnDesconectar, btnMotorApagar, btnCreditos;
    TextView tvEstado, tvBufferIn;
    ImageView imgEncender, imgDerecha, imgIzquierda;
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = null;
    /*onCreate-------------------------------------------------------------------------------------->|*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_interfaz);
        /*ENLACE DE LOS ELEMENTOS CON LAS VISTAS---------------------------------------------------->|*/
        btnIzquierda = (Button) findViewById(R.id.btnIzquierda);
        btnDerecha = (Button) findViewById(R.id.btnDerecha);
        btnDesconectar = (Button) findViewById(R.id.btnDesconectar);
        tvBufferIn = (TextView) findViewById(R.id.tvBufferIn);
        btnMotorApagar = (Button)findViewById(R.id.btnApagarMotor);
        btnCreditos = (Button)findViewById(R.id.btnCredito);
        tvEstado = (TextView)findViewById(R.id.tvEstado);
        imgEncender = (ImageView)findViewById(R.id.imgEncender);
        imgDerecha = (ImageView)findViewById(R.id.imgDerecha);
        imgIzquierda = (ImageView)findViewById(R.id.imgIzquierda);

        btnIzquierda.setOnClickListener(this);
        btnDerecha.setOnClickListener(this);
        btnMotorApagar.setOnClickListener(this);
        btnCreditos.setOnClickListener(this);
        btnDesconectar.setOnClickListener(this);
        /*HANDLER (recibir datos en la app móvil)--------------------------------------------------->|*/
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        tvBufferIn.setText("Dato: " + dataInPrint);//<-<- PARTE A MODIFICAR >->->
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            }
        };
        /*ADAPTADOR BLUETOOTH (para conectar)------------------------------------------------------->|*/
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();
    }
    /*MÉTODOS onClick------------------------------------------------------------------------------->|*/
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnIzquierda:
                MyConexionBT.write("1");
                tvEstado.setText("Motor: girando a la izquierda");
                imgDerecha.setVisibility(View.INVISIBLE);
                imgEncender.setVisibility(View.INVISIBLE);
                imgIzquierda.setVisibility(View.VISIBLE);
                break;
            case R.id.btnDerecha:
                MyConexionBT.write("2");
                tvEstado.setText("Motor: girando a la derecha");
                imgIzquierda.setVisibility(View.INVISIBLE);
                imgEncender.setVisibility(View.INVISIBLE);
                imgDerecha.setVisibility(View.VISIBLE);
                break;
            case R.id.btnApagarMotor:
                MyConexionBT.write("3");
                tvEstado.setText("Motor: apagado");
                imgIzquierda.setVisibility(View.INVISIBLE);
                imgDerecha.setVisibility(View.INVISIBLE);
                imgEncender.setVisibility(View.VISIBLE);
                break;
            case R.id.btnCredito:
                AlertDialog.Builder basico = new AlertDialog.Builder(UsuarioInterfaz.this);
                basico.setTitle("Autor:")
                        .setMessage("Cristopher D. Chavez")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(UsuarioInterfaz.this,"¡Somos castores!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
                break;
            case R.id.btnDesconectar:
                if (btSocket!=null) {
                    try {btSocket.close();}
                    catch (IOException e)
                    { Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();;}
                }
                finish();
                break;
        }
    }
    /*Se crea una CONEXIÓN DE SALIDA segura para el dispositivo usando el servicio UUID------------->|*/
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }
    /*MÉTODO onResume------------------------------------------------------------------------------->|*/
    @Override
    public void onResume() {
        super.onResume();
        /*Se obtiene la dirección MAC*/
        Intent intent = getIntent();
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        /*Intenta crear el socket de conexión bluetooth*/
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR", Toast.LENGTH_LONG).show();
        }
        /*Intenta conectar al socket*/
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }
    /*MÉTODO onPause-------------------------------------------------------------------------------->|*/
    @Override
    public void onPause() {
        super.onPause();
        /*Si la aplicación se cierra o se deja en segundo plano apaga el socket*/
        try {
            btSocket.close();
        } catch (IOException e2) {}
    }
    /*Función que verifica si el BLUETOOTH ESTÁ DISPONIBLE y SOLICITA en caso de ser necesario------>|*/
    private void VerificarEstadoBT() {
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no cuenta con bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    /*Clase que realiza todos los métodos de CONEXIÓN----------------------------------------------->|*/
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        /*Se mantiene a la espera de recibir, lo que se reciba se envía al Handler*/
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        /*Función para el envío de datos al arduino*/
        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e) {
                Toast.makeText(getBaseContext(), "Error en la conexión Bluetooth", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}

