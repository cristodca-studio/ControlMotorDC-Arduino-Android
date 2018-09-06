//Se declaran los pines de salida que le indicarán al CI el sentido del giro del motor
const int izquierda = 3;
const int derecha = 2;
int lectura = 0; //Variable que guarda los datos recibidos por el módulo Bluetooth

void setup(){
    //Se configuran los pines en modo de salida y se establecen en bajo para que al iniciar
    //el programa, el motor esté apagado
    pinMode(izquierda, OUTPUT);
    pinMode(derecha, OUTPUT);
    digitalWrite(izquierda, LOW);
    digitalWrite(derecha, LOW);
    //Se inicia el puerto serial uno en 9600.
    //El puerto serial es el uno debido a que el proyecto se realizó en un Arduino MEGA 2560 y
    //el módulo se conectará al puerto 18 (TX1) y 19 (RX1)
    Serial1.begin(9600);
  }

void loop(){
  //Si existe conexión
  if(Serial1.available()){
    //Guarda el valor leido
    lectura = Serial1.read();
    //Compara el valor leido con 1, 2 o 3
  if(lectura == '1'){
    //Configura las salidas para el estado que se desea
    digitalWrite(izquierda, HIGH);
    digitalWrite(derecha, LOW);
    Serial1.println("ESTADO: izquierda");
  }
  if(lectura == '2'){
    digitalWrite(izquierda, LOW);
    digitalWrite(derecha, HIGH);
    Serial1.println("ESTADO: derecha");
  }
  if(lectura == '3'){
    digitalWrite(izquierda, LOW);
    digitalWrite(derecha, LOW);
    Serial1.println("ESTADO: detenido");  
  } 
  }
    
}
