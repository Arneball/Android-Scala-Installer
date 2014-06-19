Installs scala library 2.11 on the rooted android phone
This is used to avoid bundling the scala library with the apk everytime we compile during development, in release you have to do a full build including the scala-library in the proguard 

to use, put the following:

    <uses-library android:name="s1"/>
    <uses-library android:name="s2"/>
    <uses-library android:name="s3"/>

in AndroidManifest.xml
