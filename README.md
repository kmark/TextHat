# TextHat - SMS on your head.
A high school senior project. Objective? Decorate the hell out of a paper [Burger King crown](http://i.imgur.com/tFgbXgE.jpg). That's boring. I put a bucket on top of the crown, covered it in aluminum foil, and stuffed an Arduino into it. Lulz ensue.

[![YOLO SWAG](https://github.com/kmark/TextHat/raw/master/DefaultMessage.jpg)](http://instagram.com/p/fg-90OomUI/)

## What you'll need…
* A paper Burger King crown (well, you don't *need* it)
* A plastic bucket that fits on your head. Got mine at Home Depot.
* An [Arduino Mega 2560](http://arduino.cc/en/Main/arduinoBoardMega2560) (because I wasn't losing more sleep on getting it working with an Uno)
* A [16x2 Character LCD w/ RGB backlight](https://www.sparkfun.com/products/10862) (the RGB backlight makes it look 10x more impressive)
* Tools. (like a drill, drill bits, rubber duct tape, electrical tape…)
* [A switch with a light.](http://www.radioshack.com/product/index.jsp?productId=3097457) (because we're in the business of cool… and convenience)
* Wire. 20 gauge works nicely. Stranded or solid core. Whatever. [These are great.](https://www.sparkfun.com/products/11026)
* [BT Shield v2.1](http://imall.iteadstudio.com/im120417006.html) *  (I used this because it's what I had on hand, feel free to use something else/better)
* [Breadboard.](https://www.sparkfun.com/products/9567) (Again, you don't *need* this)
* [4 D Battery Holder](http://www.radioshack.com/product/index.jsp?productId=2062245) (this thing will eat amp hours)
* [5/2.1mm DC power jack](https://www.sparkfun.com/products/11476) (unless you plan to use USB or something)
* An Android phone (because you couldn't *pay* me to write Objective-C, seriously)
* Some ~~creativity~~ intelligence. (because I'm not going to tell you how to do everything)

Here is the highly generalized procedure I followed to create hat. Here's a tip: Test the electronics after every step you take so you can identify when and where something went wrong. If you wait until everything is done and mounted in the hat to test it you'll find it much harder to isolate the issue.


## Step 1
Load up the Arduino with the code provided in the project. It will only work out-of-the-box if your setup is *exactly* like mine, which is unlikely. Even if you have the same hardware you'll still need to change the pin numbers.

## Step 2
If your device is running at least Android 4.3 you shouldn't have any issues as that's what I tested it with. A Galaxy S4 to be exact. If it's anything under that you'll have to change the project's build settings to work with your version of Android. I honestly don't expect this to work on anything lower than Jelly Bean without code modifications, but hell, it could.

Building this app for iOS or other devices isn't necessarily possible. You'll need to have some kind of hook for receiving text messages. With Android this is super easy and can work even when the app isn't "open" but not all mobile operating systems expose this. Maybe create a Twilio number that people can send their messages to and then gives your phone a push notification with their text (and then act on that).

## Step 3
Figure out how this is going to fit in your bucket. See [Wiring up the LCD](#wiring-up-the-lcd). Hopefully you thought about this before you bought it. Mine was just big enough to fit the Mega on an angle with the power jack. And even then it wasn't a perfect fit. This isn't a huge project so you don't need to really draw it out, but that helps. A mental picture/idea is all you need. Figure out where you're going to place every component, how they're going to connect, and how they're going to be mounted. Remember, this is going on your head so weight distribution is important. I failed to realize this early on so I had to add an uncomfortable velcro chin strap.

## Step 4
Hook everything up outside the bucket if you can. See Make sure it's working. Especially the Arduino as that's going to be a real PITA to recode if it's already mounted in your bucket. You could reprogram over Bluetooth serial but that's for another day… Send your phone a text (from itself will work) and see if the LCD display gets your message.

## Step 5
Drill any and all holes through your bucket. I used a 1-inch bit for the switch and I mounted my LCD with some duct tape and [long headers](https://www.sparkfun.com/products/10158). I didn't feel like prepping the Dremel so I took out a bit a little larger than the header size and pulled left and right to make the opening in the side of the bucket. The plastic will eventually get hot enough from the friction to make moving the bit easy. If you're going to spray paint the bucket or something do it now.

## Step 6
Put the Arduino and everything else in the bucket. Mount the LCD. Mount the switch. Try to keep the wire hookups intact while moving it in so you don't have to worry about something not working right. Solder the leads from the DC power jack (which should already be soldered and plugged into the Arduino) to the switch. I mounted my Arduino with looped duct tape on the bottom of the bucket. Also find a way to hook up your battery pack to your switch. You'll probably want that in your pocket or something so give yourself enough wire.

## Step 7
Test it out! Plug in the batteries and flip the switch! Let's hope everything works. If it doesn't, verify your connections and think about what you did between now and your last working test. If your Arduino is powering up, Bluetooth is working, the colors are changing on the LCD, but the characters are on the LCD aren't showing up 


## Wiring up the LCD
I used [this guide by Adafruit](http://learn.adafruit.com/character-lcds/rgb-backlit-lcds) to figure it out. You don't need the potentiometer for contrast control, but you do need to use the pin. I pulled it to ground for maximum contrast, if I remember correctly. LCDs are really fun to use with Arduino projects. They're inexpensive and highly reusable. It's also "fun" to design a user interface using only a certain set of characters and very limited fix-width space. My pin assignments were like this. I started counting LCD pin numbers on the side they were closest to. In other words, pin 1 is the pin that's closest to the edge of the LCD board. You can definitely hook this up to an Uno but getting it work with your custom interrupts/timers will prove to be a challenge. Due to time constraints I avoided the issue and used the larger Arduino Mega with more PWM pins and interrupts. The extra hardware serials will come in handy depending on your Bluetooth module.

    LCD Pin    Purpose/Arduino Pin 
    1          GND
    2          +5V
    3          0-5V (Contrast)
    4          Pin 30
    5          GND
    6          Pin 31
    7          None
    8          None
    9          None
    10         None
    11         Pin 6
    12         Pin 7
    13         Pin 8
    14         Pin 22
    15         +5V
    16         Pin 44 (R)
    17         Pin 45 (G)
    18         Pin 46 (B)
 
## The Bluetooth Serial Comm. Protocol
I set it up so messages work like this:

    Format:
        ASCII string of sender name or phone number\nASCII string of text message\n
    Examples:
        Kevin Mark\nHello World!\n
        555-123-4567\nWassup?\n
        This will be shown and scrolled on the first line of the LCD\nSecond line\n

You can change the delimiter to whatever you want but I'd recommend something non-printing. The one I hard-coded in is the line feed (LF) character. That's 0xA in ASCII shorthand hex. Obviously you don't want to literally send "\n" you want to send what it *means*. Messages only flow in one direction and that's from the phone to the Arduino. The phone must be already paired with the Bluetooth module and the Bluetooth name must be "arduino" for the Android code to work out-of-the-box. The Android app whitelists a set of characters I manually tested and strips everything else out of the message sent over Bluetooth. If the Arduino code's buffer gets messed up somehow (LCD display acts up) a quick reboot (of the Arduino) will fix it until it happens again. The Arduino code will trust anything sent to it so all processing must be done on the Android end.

## Why is the default message #YOLO #SWAG ?
It seemed like a good idea at the time. #SWAG scrolls across the bottom for additional lulz. Also high school.

## More Photos
![Just finished](https://github.com/kmark/TextHat/raw/master/DatHat.jpg)
![During a demo with a friend](https://github.com/kmark/TextHat/raw/master/SuchKrowne.jpg)

Also [here's a little clip](http://instagram.com/p/flN8yrImav/) I put together.


## License
TextHat's Android and Arduino code are licensed under the GNU-GPLv3. I don't have a patent on a device that shows text messages on top of your head so tell Apple to get on that. See LICENSE.md for more information.
