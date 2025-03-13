## Example of creating a circular ProgressBar. 

![ProgressBarCircle1](https://github.com/user-attachments/assets/58d396bf-9b92-4a10-bdd1-3458388eefe5)
![ProgressBarCircle2](https://github.com/user-attachments/assets/6eaeb215-c2ce-41ab-be3d-fe2a364d3757)

Open source. To use the ready-made library, add the dependency:
```
dependencies {

    implementation("io.github.uratera:progress_bar:1.1.0")
}
```
### Attributes
|Attributes          |Description    |Default value
|--------------------|---------------|-------------|
pbc_arcBlurStyle     |Style of blur  |normal
pbc_arcBlurWidth     |Arc blur width |0
pbc_arcEndColor      |Color of the end of the arc |black
pbc_arcGradientStyle |Style of gradient |sweep
pbc_arcGroundWidth   |Width of the background arc |10dp
pbc_arcProgressWidth |Width of the arc of progress |10dp
pbc_arcRoundShow     |Show arc fillets |false
pbc_circleStroke     |Width of the line of the circle |0dp
pbc_dashHeight       |Dash height    |10dp
pbc_dashRadius       |Dash corner radius |0dp
pbc_dashWidth        |Dash width     |3dp
pbc_progressColor    |Color of progress |black
pbc_progressStyle    |Style of progress |dash
pbc_groundColor      |Progress background color |gray
pbc_fontFamily       |Font           |default
pbc_percentShow      |Show value as a percentage |true
pbc_textColor        |Text color     |black
pbc_textSize         |Text size      |14sp
pbc_value            |Current value  |0
pbc_valueMax         |Maximum value  |40
pbc_warningColor1    |Color of the first warning |yellow
pbc_warningColor2    |Color of the second warning |red
pbc_warningShow      |Show color warnings |false
pbc_warningValue1    |First warning value, %|80
pbc_warningValue2    |The second value of warning, %|90

**Styles of blur**
- normal
- solid

**Styles of gradient**
- sweep
- radial

**Styles of progress**
- dash
- arc
- solid

Usage:
```
<com.tera.progressbar.ProgressBarCircle
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

