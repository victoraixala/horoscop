package com.aldarius.horoscop

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class FragmentMissatge : DialogFragment() {

    /*
    public static FragmentMissatge newInstance(String missatge) {
        FragmentMissatge frag = new FragmentMissatge();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }
*/
    companion object {
        private const val MISSATGE = "missatge"
        fun newInstance(missatge: Int): FragmentMissatge {
            val frag = FragmentMissatge()
            val args = Bundle().apply {
                missatge.let { putInt("missatge", it) }
            }
            frag.arguments = args
            return frag
        }
    }

    //var onClick: (() -> Unit)? = null
    lateinit var onClick: DialogInterface.OnClickListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(activity!!)
        builder.setMessage(arguments?.getInt("missatge")!!)
            //.setPositiveButton("OK", DialogInterface.OnClickListener(function = parentFragment))
            //.setPositiveButton("OK", DialogInterface.OnClickListener() { dialog, id ->
//                fun onClick (dialog: DialogInterface , id: Int ) {
//                    dialog.dismiss()
//                }
//                })
        return builder.create()
    }
}// Empty constructor required for DialogFragment