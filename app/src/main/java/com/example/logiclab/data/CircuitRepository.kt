package com.example.logiclab.data

import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

data class CircuitData(
    val components: List<CircuitComponent>,
    val wires: List<CircuitWire>
)

object CircuitSerializer {

    fun serialize(components: List<CircuitComponent>, wires: List<CircuitWire>): String {
        val root = JSONObject()
        root.put("version", 1)

        val compArr = JSONArray()
        for (c in components) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("type", c.type)
            obj.put("x", c.x.toDouble())
            obj.put("y", c.y.toDouble())
            obj.put("rotation", c.rotation)
            obj.put("label", c.label)
            obj.put("switchState", c.switchState)
            obj.put("clockHz", c.clockHz.toDouble())
            compArr.put(obj)
        }
        root.put("components", compArr)

        val wireArr = JSONArray()
        for (w in wires) {
            val obj = JSONObject()
            obj.put("id", w.id)
            obj.put("fromComponentId", w.fromComponentId)
            obj.put("fromPortId", w.fromPortId)
            obj.put("toComponentId", w.toComponentId)
            obj.put("toPortId", w.toPortId)
            wireArr.put(obj)
        }
        root.put("connections", wireArr)

        return root.toString(2)
    }

    fun deserialize(jsonStr: String): CircuitData {
        val root = JSONObject(jsonStr)
        val compArr = root.optJSONArray("components") ?: JSONArray()
        val components = mutableListOf<CircuitComponent>()

        for (i in 0 until compArr.length()) {
            val obj = compArr.getJSONObject(i)
            components.add(
                CircuitComponent(
                    id = obj.getString("id"),
                    type = obj.getString("type"),
                    x = obj.optDouble("x", 100.0).toFloat(),
                    y = obj.optDouble("y", 100.0).toFloat(),
                    rotation = obj.optInt("rotation", 0),
                    label = obj.optString("label", ""),
                    switchState = obj.optBoolean("switchState", false),
                    clockHz = obj.optDouble("clockHz", 1.0).toFloat()
                )
            )
        }

        val wireArr = root.optJSONArray("connections") ?: JSONArray()
        val wires = mutableListOf<CircuitWire>()

        for (i in 0 until wireArr.length()) {
            val obj = wireArr.getJSONObject(i)
            wires.add(
                CircuitWire(
                    id = obj.optString("id", "w_$i"),
                    fromComponentId = obj.getString("fromComponentId"),
                    fromPortId = obj.getString("fromPortId"),
                    toComponentId = obj.getString("toComponentId"),
                    toPortId = obj.getString("toPortId")
                )
            )
        }

        return CircuitData(components, wires)
    }
}

class CircuitRepository(private val dao: CircuitDao) {

    val allCircuits: Flow<List<CircuitEntity>> = dao.getAllCircuits()

    suspend fun saveCircuit(name: String, description: String, components: List<CircuitComponent>, wires: List<CircuitWire>): Long {
        val json = CircuitSerializer.serialize(components, wires)
        val entity = CircuitEntity(
            name = name,
            description = description,
            circuitJson = json,
            updatedAt = System.currentTimeMillis()
        )
        return dao.insertCircuit(entity)
    }

    suspend fun loadCircuit(id: Long): CircuitData? {
        val entity = dao.getCircuitById(id) ?: return null
        return CircuitSerializer.deserialize(entity.circuitJson)
    }

    suspend fun deleteCircuit(id: Long) {
        dao.deleteCircuitById(id)
    }
}
