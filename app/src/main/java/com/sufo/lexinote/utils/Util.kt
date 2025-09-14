package com.sufo.lexinote.utils

import com.sufo.lexinote.data.local.db.entity.DictWord

/**
 * Created by sufo on 2025/8/4 01:16.
 *
 */

val TERMS = mapOf("zk" to "中考", "gk" to "高考", "ky" to "考研","cet4" to "四级","cet6" to "六级","toefl" to "托福","ielts" to "雅思","gre" to "GRE")
val TAGS = arrayOf("zk", "gk", "ky", "cet4", "cet6", "toefl", "ielts", "gre")
val EXCHANGES = mapOf(
    "p" to "过去式", "d" to "过去分词",
    "i" to "现在分词", "3" to "第三人称单数",
    "r" to "比较级", "t" to "比较级",
    "s" to "复数", "0" to "原型","1" to "类别",
)

//fun getTagNames(tag:String): MutableList<String>{
//    val tags = tag.split(" ")
//    val names = mutableListOf<String>()
//    tags.forEach { t ->
//        TERMS[t]?.let { names.add(it) }
//    }
//    return names
//}
//更简洁
fun getTagNames(tag: String): List<String> {
    return tag.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull { t -> TERMS[t] }
}

fun wordExchange (data: DictWord) :Map<String,String>{
    val lines = mutableMapOf<String,String>()
    val exchange = data.exchange
    if(exchange.isNullOrBlank()){
        return lines
    }
    val exchgs = exchangeLoads(exchange)
    if(exchgs.isEmpty()){
        return lines
    }
    var count = 0
    var last = ""
    var part = mutableListOf<String>()
    listOf("p","d","i","3").forEach {
        val p  = exchgs[it]
        if(p != null){
            count++
            if(p != last){
                part.add(p)
            }
        }
    }
    val text = if(count < 4) "" else part.joinToString(separator = ", ")
//    var origin = ""
    var t = exchgs["0"]
    if(t !=null && t.lowercase() == data.word){
        exchgs.remove("0")
        if(exchgs.containsKey("1")){
            exchgs.remove("1")
        }
    }
//    if(exchgs.containsKey("0")){
//        if(t != data.word){
//            val origin = t
//            var derive = ""
//            if(exchgs.containsKey("1")){
//                t = exchgs["1"]
//
//                if()
//            }
//
//
//
//        }
//    }

    var better = ""
    if(exchgs.containsKey("r") and exchgs.containsKey("t")){
        better = exchgs["r"] + ", " + exchgs["t"]
    }

    if(text.isNotEmpty()){
        lines["tense"] = (text)
    }
    if(better.isNotEmpty() && arrayOf("r","t").contains(exchgs.getOrDefault("1",""))){
        lines["comparative"] = (better)
    }
    return lines
}

fun exchangeLoads (exchg:String): MutableMap<String,String>{
    val obj = mutableMapOf<String,String>()
    exchg.split("/").forEach {
        val pos = it.indexOf(":")
        if(pos < 0){
            return@forEach
        }
        val k = it.substring(0..pos-1).trim()
        val v = it.substring(pos+1).trim()
        obj[k] = v
    }
    return obj
}