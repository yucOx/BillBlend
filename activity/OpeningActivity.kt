
class OpeningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.opening_activity)

        Handler().postDelayed(
            {
                var intent = Intent(this@OpeningActivity,LoginActivity::class.java)
                startActivity(intent)
                finish()
            },3000)

    }
}
