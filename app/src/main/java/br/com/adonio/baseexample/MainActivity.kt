package br.com.adonio.baseexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import br.com.adonio.promise.Promise
import br.com.adonio.task.Task

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Promise<Boolean> { resolve, reject ->
            resolve(true)
        }.then {
            Log.i("Adon", "then")
        }
        .always {
            Log.i("Adon", "always")
        }
    }
}
