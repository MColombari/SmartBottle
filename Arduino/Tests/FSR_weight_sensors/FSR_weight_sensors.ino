#define IN_PIN A1
#define PRINT_PERIOD 100

unsigned long start_time;

float values[100];    // Digital filter.

int index;

double means(){
  double ret = 0;
  for(int i = 0; i < 100; i++){
    ret += values[i];
  }
  return ret / 100;
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  start_time = millis();
  index = 0;
  for (int i = 0; i < 100; i++){
    values[i] = 0;
  }
}

void loop() {
  if(index >= 100){
    index = 0;
  }
  values[index] = analogRead(IN_PIN);
  index++;
  // put your main code here, to run repeatedly:
  if(PRINT_PERIOD <= millis() - start_time){
    double value = means();
    Serial.println(value);
    start_time = millis();
  }
}
