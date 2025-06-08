package com.alexwawo.w08firebase101

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StudentViewModel : ViewModel() {
    private val db = Firebase.firestore
    var students by mutableStateOf(listOf<Student>())
        private set

    init {
        fetchStudents()
    }

    fun addStudent(student: Student) {
        val studentMap = hashMapOf(
            "id" to student.id,
            "name" to student.name,
            "program" to student.program
        )

        db.collection("students")
            .add(studentMap)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot added with ID: ${it.id}")
                fetchStudents() // Refresh list
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    fun updateStudent(student: Student) {
        val studentMap = mapOf(
            "id" to student.id,
            "name" to student.name,
            "program" to student.program
        )
        val studentDocRef = db.collection("students").document(student.docId)
        studentDocRef.set(studentMap)
            .addOnSuccessListener {
                val phonesRef = studentDocRef.collection("phones")
                // Step 1: Delete old phones
                phonesRef.get().addOnSuccessListener { snapshot ->
                    val deleteTasks = snapshot.documents.map {
                        it.reference.delete() }
                    // Step 2: When all phones deleted, add new phones

                    com.google.android.gms.tasks.Tasks.whenAllComplete(deleteTasks)
                        .addOnSuccessListener {
                            val addPhoneTasks = student.phones.map { phone ->
                                val phoneMap = mapOf("number" to phone)
                                phonesRef.add(phoneMap)
                            }
                            // Step 3: After all new phones added, fetch
                            updated list

                                    com.google.android.gms.tasks.Tasks.whenAllComplete(addPhoneTasks)
                                        .addOnSuccessListener {
                                            fetchStudents()
                                        }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating student", e)
            }
    }


    fun deleteStudent(student: Student) {
        db.collection("students").document(student.docId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Student deleted")
                fetchStudents()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error deleting student", it)
            }
    }


    private fun fetchStudents() {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Student>()
                for (document in result) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val program = document.getString("program") ?: ""
                    val docId = document.id
                    list.add(Student(id, name, program, docId))
                }
                students = list
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
            }
    }
}

