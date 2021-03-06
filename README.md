LineAPI4J
=========

*LineAPI4J* is a library written in Java with compressed thrift client.

You might wonder what do you need it, when you already have LINE client installed on your PC/MAC/Mobile devices.

*LineAPI4J* allows you to build your own line bot, you're able to `send messages, stickers and images` with it.

Inspired by [LINE](https://github.com/carpedm20/line), LINE API written in Python by [carpedm20](https://github.com/carpedm20).

# Using LINE API

Basic process as following code.

```
$ git clone git://github.com/cslinmiso/LineAPI4J.git
...

    LineApi api = new LineApiImpl();
      try {
        LoginResult result = api.login("xxxx@xxx.com", "xxxxxxx"); // login
        LineClient client = new LineClient(api); //init client
        LineContact someoneContact = client.getContactByName("Someone"); // find contact
        System.out.println(someoneContact.sendSticker("13", "1", "100", ""));
        System.out.println(someoneContact.sendMessage(":) test"));
      } catch (java.net.SocketTimeoutException e) {
        // setAwaitforVerify false
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
```

Once you have LineContact, you can start from there.

see example App.java in io.cslinmiso.line.LineAPI4J

* *TODO fix some problems*
* *TODO update usage of api*

ScreenShot
========
![List of contacts.](http://cslinmiso.github.io/img/LineAPI4J/LineAPI4J.png)

License
========
This project is under [MIT license](http://www.opensource.org/licenses/mit-license.php).

The MIT License (MIT)

Copyright (c) 2014 Trey Lin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

Author
========
Trey Lin 

[Site](http://cslinmiso.github.io/)

[@cslinmiso](https://twitter.com/cslinmiso)
