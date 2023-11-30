#define IN_PIN_0 A0
#define IN_PIN_1 A1
#define IN_PIN_2 A2
#define IN_PIN_3 A3
#define FILTER_DIMENSION 100
#define PRINT_PERIOD 1000

/*
  Description: for the breadbord test i chose for each resistor a 10K pull-up resistor.
*/

unsigned long start_time;

float values_0[FILTER_DIMENSION];    // Digital filter 0.
float values_1[FILTER_DIMENSION];    // Digital filter 1.
float values_2[FILTER_DIMENSION];    // Digital filter 2.
float values_3[FILTER_DIMENSION];    // Digital filter 3.

int index;

double means(float in[FILTER_DIMENSION]){
  double ret = 0;
  for(int i = 0; i < FILTER_DIMENSION; i++){
    ret += in[i];
  }
  return ret / FILTER_DIMENSION;
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  start_time = millis();

  index = 0;

  for (int i = 0; i < FILTER_DIMENSION; i++){
    values_0[i] = 0;
    values_1[i] = 0;
    values_2[i] = 0;
    values_3[i] = 0;
  }
}

void loop() {
  if(index >= FILTER_DIMENSION){
    index = 0;
  }
  values_0[index] = analogRead(IN_PIN_0);
  values_1[index] = analogRead(IN_PIN_1);
  values_2[index] = analogRead(IN_PIN_2);
  values_3[index] = analogRead(IN_PIN_3);
  index++;
  // put your main code here, to run repeatedly:
  if(PRINT_PERIOD <= millis() - start_time){
    Serial.print(means(values_0)); Serial.print(",");
    Serial.print(means(values_1)); Serial.print(",");
    Serial.print(means(values_2)); Serial.print(",");
    Serial.println(means(values_3));
    start_time = millis();
  }
}
