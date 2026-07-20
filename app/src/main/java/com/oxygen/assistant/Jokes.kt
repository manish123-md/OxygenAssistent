package com.oxygen.assistant

object Jokes {
    private val list = listOf(
        "Teacher ne pucha - tum late kyu ho? Bachcha bola - Sir, jab main ghar se nikla tab ek board tha 'School Ahead Go Slow'.",
        "Ek aadmi doctor ke paas gaya, bola - Doctor sahab bhoolne ki bimari hai. Doctor bola - kab se hai? Aadmi bola - kya kab se hai?",
        "Pappu interview me gaya. HR ne pucha - weakness kya hai? Pappu bola - honesty. HR bola - ye weakness nahi hai. Pappu bola - mujhe koi farak nahi padta aap maano ya na maano.",
        "Santa se pucha gaya English me ek sentence banao jisme 'beautiful' word ho do baar. Santa bola - meri beautiful biwi itni beautiful hai ki main use dekhta hi reh jata hoon.",
        "Teacher: बताओ 'i before e except after c' ka matlab kya hai? Student: Sir matlab ye hai ki spelling hamesha galat hi hogi.",
        "Ek aadmi bola - mujhe cardio nahi karna, main already stressed hoon. Doctor bola - to phir walk kar le. Aadmi bola - walk bhi cardio hi hai na?",
        "Wife: tumhe pata hai pados wale uncle roz subah jogging karte hai. Husband: haan pata hai, unki body dekhi hai maine, wo bhi meri jaisi hi hai."
    )

    fun random(): String = list.random()
}
