// include the library code:
#include <LiquidCrystal.h>
#include <Wire.h>
 
#define REDLITE 44
#define GREENLITE 45
#define BLUELITE 46
 
// initialize the library with the numbers of the interface pins
LiquidCrystal lcd(30, 31, 6, 7, 8, 22);

const int numColors = 255;
int counter = 0;
unsigned int interruptCount = 0;
unsigned int rainbowCount = 0;
unsigned int holdAtFirstCount[2] =  {0, 0};
unsigned int topPosition = 0;
unsigned int bottomPosition = 0;
String topString = NULL;
String bottomString = NULL;
 
void setup() {
  Serial.begin(9600);
  // set up the LCD's number of rows and columns: 
  lcd.begin(16, 2);
  // Print a message to the LCD.
  
  pinMode(REDLITE, OUTPUT);
  pinMode(GREENLITE, OUTPUT);
  pinMode(BLUELITE, OUTPUT);
  //setBacklight(50, 0, 0);
  textToLCD("#YOLO", "                #SWAG        ");
  
  /* Initialize the data output interrupt. It'll fire every millisecond
     * * Timer2 is 8-bits wide, so it can count to 255
     * * 255 - 130 gives us 125 cycles left
     * * (125 cycles) * (128 prescaler) / (16MHz clock speed) = 1ms
     * * Setting the prescalar resets the interrupt
     * * We must reset the count and overflow flag after each interrupt
     * * http://arduinomega.blogspot.com/2011/05/timer2-and-overflow-interrupt-lets-get.html
     */
  TCCR2A = 0; // Wave gen mode normal
  TCCR2B = 0; // Disable
  TCNT2 = 130; // set timer count to 130 of 255
  TIFR2 = 0; // Clear overflow flag
  TIMSK2 = 1; // Enable timer compare interrupt
  TCCR2B = 5; // Timer prescaler to 128
}
 
 
void loop() {
  String serialString = "";
  boolean workingOnSecondParam = false;
  String workingFirstParam = "";
  char delim = 0xA;
  while(1) {    
    if(!Serial.available()) {
      workingOnSecondParam = false;
      serialString = "";
      //rainbowLoop();
      continue;
    }
    
    char c = Serial.read();
    if(!workingOnSecondParam && c == delim) {
      workingOnSecondParam = true;
      workingFirstParam = serialString;
      serialString = "";
    } else if(c == delim) {
      textToLCD(workingFirstParam, serialString);
      workingOnSecondParam = false;
      serialString = "";
    } else {
      serialString.concat(c);
    }
    
    delay(10); // Fucking race condition?
    
    //textToLCD("LOL", readStringFromSerial());
    //Serial.flush();
  }
}

void textToLCD(String contact, String message) {
  int length[2] = {contact.length(), message.length()};
  for(int i = 0; i < 2; i++) {
    if(length[i] == 16) {
      if(i == 0) { topString = NULL; } else { bottomString = NULL; }
      lcd.setCursor(0, i);
      lcd.print(i == 0 ? contact : message);
    } else if(length[i] < 16) {
      if(i == 0) { topString = NULL; } else { bottomString = NULL; }
      int totalPadding = 16 - length[i];
      float bothPadding = totalPadding / 2.0;
      if(totalPadding % 2 == 0) {
        String padding = "";
        for(int iP = 0; iP < bothPadding; iP++) {
          padding += " ";
        }
        lcd.setCursor(0, i);
        lcd.print(padding + (i == 0 ? contact : message) + padding);
      }
      int paddingLeft = floor(bothPadding);
      int paddingRight = ceil(bothPadding);
      lcd.setCursor(0, i);
      lcd.print(charRepeat(' ', paddingLeft) + (i == 0 ? contact : message) + charRepeat(' ', paddingRight));
    } else {
      if(i == 0) {
        topPosition = 0;
        topString = contact;
      } else {
        bottomPosition = 0;
        bottomString = message;
      }
    }
  }
}

// Fires every millisecond
ISR(TIMER2_OVF_vect) {
  interruptCount++;
  rainbowCount++;
  if(rainbowCount > 80) {
    rainbowCount = 0;
    rainbowLoop();
  }
  if(interruptCount > 500) {
    interruptCount = 0;
    if(topString != NULL) {
      if(topPosition == 1 && holdAtFirstCount[0] < 2) {
        holdAtFirstCount[0]++;
      } else {
        String extra = "";
        if(16 + topPosition >= topString.length()) {
          extra = charRepeat(' ', 16 + topPosition - topString.length());
        }
        lcd.setCursor(0, 0);
        lcd.print(topString.substring(0 + topPosition, 16 + topPosition) + extra);
        if(16 + topPosition < topString.length() + 3) {
          topPosition++;
        }
        else {
          topPosition = 0;
        }
      }
    }
    if(bottomString != NULL) {
      if(bottomPosition == 1 && holdAtFirstCount[1] < 2) {
        holdAtFirstCount[1]++;
      }
      else {
          String extra = "";
          if(16 + bottomPosition >= bottomString.length()) {
            extra = charRepeat(' ', 16 + bottomPosition - bottomString.length());
          }
        lcd.setCursor(0, 1);
        lcd.print(bottomString.substring(0 + bottomPosition, 16 + bottomPosition) + extra);
        if(16 + bottomPosition < bottomString.length() + 3) {
          bottomPosition++;
        }
        else {
          bottomPosition = 0;
          holdAtFirstCount[1] = 0;
        }
      }
    }
  }
  TCNT2 = 130; // Reset timer to 130 of 255
  TIFR2 = 0; // Clear overflow flag
}

String charRepeat(char c, int n) {
  char rc[n + 1];
  for(int i = 0; i < n; i++) {
    rc[i] = c;
  }
  return String(rc);
}

void rainbowLoop() {
  
  // This part takes care of displaying the
  // color changing in reverse by counting backwards if counter
  // is above the number of available colors  
  float colorNumber = counter > numColors ? counter - numColors: counter;
  
  // Play with the saturation and brightness values
  // to see what they do
  float saturation = 0.9; // Between 0 and 1 (0 = gray, 1 = full color)
  float brightness = 1.0; // Between 0 and 1 (0 = dark, 1 is full brightness)
  float hue = (colorNumber / float(numColors)) * 360; // Number between 0 and 360
  long color = HSBtoRGB(hue, saturation, brightness);
  
  // Get the red, blue and green parts from generated color
  int red = color >> 16 & 255;
  int green = color >> 8 & 255;
  int blue = color & 255;

  setBacklight(red, green, blue);
  
  // Counter can never be greater then 2 times the number of available colors
  // the colorNumber = line above takes care of counting backwards (nicely looping animation)
  // when counter is larger then the number of available colors
  counter = (counter + 1) % (numColors * 2);
  
  // If you uncomment this line the color changing starts from the
  // beginning when it reaches the end (animation only plays forward)
  // counter = (counter + 1) % (numColors);

  //delay(80);
}

long HSBtoRGB(float _hue, float _sat, float _brightness) {
    float red = 0.0;
    float green = 0.0;
    float blue = 0.0;
    
    if (_sat == 0.0) {
        red = _brightness;
        green = _brightness;
        blue = _brightness;
    } else {
        if (_hue == 360.0) {
            _hue = 0;
        }

        int slice = _hue / 60.0;
        float hue_frac = (_hue / 60.0) - slice;

        float aa = _brightness * (1.0 - _sat);
        float bb = _brightness * (1.0 - _sat * hue_frac);
        float cc = _brightness * (1.0 - _sat * (1.0 - hue_frac));
        
        switch(slice) {
            case 0:
                red = _brightness;
                green = cc;
                blue = aa;
                break;
            case 1:
                red = bb;
                green = _brightness;
                blue = aa;
                break;
            case 2:
                red = aa;
                green = _brightness;
                blue = cc;
                break;
            case 3:
                red = aa;
                green = bb;
                blue = _brightness;
                break;
            case 4:
                red = cc;
                green = aa;
                blue = _brightness;
                break;
            case 5:
                red = _brightness;
                green = aa;
                blue = bb;
                break;
            default:
                red = 0.0;
                green = 0.0;
                blue = 0.0;
                break;
        }
    }

    long ired = red * 255.0;
    long igreen = green * 255.0;
    long iblue = blue * 255.0;
    
    return long((ired << 16) | (igreen << 8) | (iblue));
}
 
 
 
void setBacklight(uint8_t r, uint8_t g, uint8_t b) {
  // normalize the red LED - its brighter than the rest!
  //r = map(r, 0, 255, 0, 100);
  //g = map(g, 0, 255, 0, 150);
 
  //r = map(r, 0, 255, 0, brightness);
  //g = map(g, 0, 255, 0, brightness);
  //b = map(b, 0, 255, 0, brightness);
 
  // common anode so invert!
  //r = map(r, 0, 255, 255, 0);
  //g = map(g, 0, 255, 255, 0);
  //b = map(b, 0, 255, 255, 0);
  //Serial.print("R = "); Serial.print(r, DEC);
  //Serial.print(" G = "); Serial.print(g, DEC);
  //Serial.print(" B = "); Serial.println(b, DEC);
  analogWrite(REDLITE, r);
  analogWrite(GREENLITE, g);
  analogWrite(BLUELITE, b);
}
