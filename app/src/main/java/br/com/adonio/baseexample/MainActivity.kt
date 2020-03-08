package br.com.adonio.baseexample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.com.adonio.promise.promise
import br.com.adonio.promise.promiseIt
import br.com.adonio.task.Task
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ui_text.text = ""

        Task.main(2333) {
            Log.i("Adon", "s1mple")
            ui_text.text = ui_text.text.toString() + "\ns1mple"
        }

        promise<Boolean> {
            resolve(true)
            ui_text.text = ui_text.text.toString() + "\nresolve(true)"
        }.then {
            Log.i("Adon", "then")
            ui_text.text = ui_text.text.toString() + "\nthen"
        }
        .catch {
            Log.i("Adon", "never call 1")
            ui_text.text = ui_text.text.toString() + "\ncatch"
        }
        .always {
            Log.i("Adon", "always")
            ui_text.text = ui_text.text.toString() + "\nalways"
        }

        promiseIt(22)
            .then {
                Log.i("Adon", "$it")
                ui_text.text = ui_text.text.toString() + "\n\"$it\""
            }
            .catch {
                Log.i("Adon", "never call")
                ui_text.text = ui_text.text.toString() + "\nnever call"
            }
            .always {
                Log.i("Adon", "always int")
                ui_text.text = ui_text.text.toString() + "\nalways int"
            }
    }
}
