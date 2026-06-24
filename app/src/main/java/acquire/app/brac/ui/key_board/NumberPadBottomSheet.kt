package acquire.app.brac.ui.key_board

import com.zztl.pos.ucb.R
import acquire.app.brac.ui.base.BaseBottomSheet
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import java.util.Locale

open class NumberPadBottomSheet : BaseBottomSheet(), View.OnClickListener {

    private var mExInputPin: EditText? = null
    private var mTvPosition1: TextView? = null
    private var mTvPosition2: TextView? = null
    private var mTvPosition3: TextView? = null
    private var mTvPosition4: TextView? = null
    private var mTvPosition5: TextView? = null
    private var mTvPosition6: TextView? = null
    private var mTvPosition7: TextView? = null
    private var mTvPosition8: TextView? = null
    private var mTvPosition9: TextView? = null
    private var mTvPosition10: TextView? = null


    override fun getTheme(): Int = R.style.BottomSheetDialogStyle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.core_bottom_sheet_number_pad_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialView(view)

    }

    private fun initialView(view: View) {

        view.findViewById<View>(R.id.tvPosition1).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition2).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition3).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition4).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition5).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition6).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition7).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition8).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition9).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPosition10).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPositionDelete).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPositionOk).setOnClickListener(this)
        view.findViewById<View>(R.id.tvPositionCancel).setOnClickListener(this)

        val hashButton = view.findViewById<TextView>(R.id.tvPositionHash)
        val oubleZero = view.findViewById<TextView>(R.id.tvPositionDouble0)
        hashButton.isEnabled = mIsReadyForPin
        oubleZero.isEnabled = mIsReadyForPin
        if(mIsReadyForPin){
            view.findViewById<View>(R.id.tvPositionHash).setOnClickListener(this)

            hashButton.setText("#")

            view.findViewById<View>(R.id.tvPositionDouble0).setOnClickListener(this)
            oubleZero.setText("00")
        }else{
            hashButton.setText("")
            oubleZero.setText("")

        }


        mExInputPin = view.findViewById<EditText>(R.id.exInputPin)
        mTvPosition1 = view.findViewById<TextView>(R.id.tvPosition1)
        mTvPosition2 = view.findViewById<TextView>(R.id.tvPosition2)
        mTvPosition3 = view.findViewById<TextView>(R.id.tvPosition3)
        mTvPosition4 = view.findViewById<TextView>(R.id.tvPosition4)
        mTvPosition5 = view.findViewById<TextView>(R.id.tvPosition5)
        mTvPosition6 = view.findViewById<TextView>(R.id.tvPosition6)
        mTvPosition7 = view.findViewById<TextView>(R.id.tvPosition7)
        mTvPosition8 = view.findViewById<TextView>(R.id.tvPosition8)
        mTvPosition9 = view.findViewById<TextView>(R.id.tvPosition9)
        mTvPosition10 = view.findViewById<TextView>(R.id.tvPosition10)

        setPositionsValue()

    }


    private fun setPositionsValue() {
        var numbers = (1..9).toMutableList()
        if(mIsNumberShuffle){
            numbers.clear()
            numbers = (0..9).toMutableList()
        }else{
            numbers.add(0)
        }
        //numbers.shuffle()
        val textViews = arrayOf(
            mTvPosition1,
            mTvPosition2,
            mTvPosition3,
            mTvPosition4,
            mTvPosition5,
            mTvPosition6,
            mTvPosition7,
            mTvPosition8,
            mTvPosition9,
            mTvPosition10
        )
        for ((index, number) in numbers.withIndex()) {
            textViews[index]?.text = "$number"
        }
    }

    override fun onClick(view: View) {
        try {
            when (view.id) {
                R.id.tvPositionOk -> {

                    var isTakeAction = false
                    var inputValue = mExInputPin?.text.toString()

                    if(inputValue!=null && inputValue.length<=4){
                        inputValue = inputValue.replace(",","")
                        try{
                            val amount = inputValue.toDouble()
                            if(amount>0){
                                isTakeAction = true
                            }
                        }catch (ex: Exception){
                        }
                        }
                    dismiss()
                    onClickedSubmit(mExInputPin?.text.toString(),isTakeAction)
                }
                R.id.tvPositionDouble0 -> {
                    setDisplay("0")
                    setDisplay("0")
                }
                R.id.tvPositionHash -> {
                    setDisplay("#")
                }

                R.id.tvPositionCancel -> {
                    clearAllCloseDisplay()
                }
                R.id.tvPositionDelete -> {
                    clearDeleteDisplay()
                }

                R.id.tvPosition1 -> {
                    setDisplay(mTvPosition1)
                }

                R.id.tvPosition2 -> {
                    setDisplay(mTvPosition2)
                }

                R.id.tvPosition3 -> {
                    setDisplay(mTvPosition3)
                }

                R.id.tvPosition4 -> {
                    setDisplay(mTvPosition4)
                }

                R.id.tvPosition5 -> {
                    setDisplay(mTvPosition5)
                }

                R.id.tvPosition6 -> {
                    setDisplay(mTvPosition6)
                }

                R.id.tvPosition7 -> {
                    setDisplay(mTvPosition7)
                }

                R.id.tvPosition8 -> {
                    setDisplay(mTvPosition8)
                }

                R.id.tvPosition9 -> {
                    setDisplay(mTvPosition9)
                }

                R.id.tvPosition10 -> {
                    setDisplay(mTvPosition10)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
//        dismiss()
    }

    private fun setDisplay(mTvPosition: TextView?) {
        var inVal = mExInputPin?.text.toString() ?: ""
        if(inVal.length==10){return}
        inVal = onInputReceived(inVal+mTvPosition?.text.toString())

        mExInputPin?.setText(inVal)
        onClickedSubmit(inVal)
    }

    private fun setDisplay(inVal2: String) {
        var inVal = mExInputPin?.text.toString() ?: ""
        if(inVal.length==10){return}

        val inValUpdate = onInputReceived(inVal+inVal2)
        mExInputPin?.setText(inValUpdate)
        onClickedSubmit(inValUpdate)
    }

    fun onInputReceived(input: String): String {

        // 1. Remove non-digits (to prevent issues with commas/dots)
        val cleanString = input.replace(Regex("[^\\d]"), "")

        if (cleanString.isEmpty()) return "0.00"

        // 2. Convert to Double and shift decimals
        val parsed = cleanString.toDouble()
        val formattedValue = parsed / 100

        // 3. Format with commas and 2 decimal places
        return NumberFormat.getCurrencyInstance(Locale.US).apply {
            // Remove currency symbol ($) if you only want the numbers
            val symbols = (this as DecimalFormat).decimalFormatSymbols
            symbols.currencySymbol = ""
            this.decimalFormatSymbols = symbols
        }.format(formattedValue).trim()
    }

    private fun clearAllCloseDisplay(){
        dismiss()
        onClickedSubmit("0.00")
    }

    private fun clearDeleteDisplay() {
        var inText = mExInputPin?.text?.toString() ?: ""
//        var newText ="0.00"
//        if (inText.length > 1) {
//            newText = inText.substring(0, inText.length - 1)
//            mExInputPin?.setText(newText)
//        } else {
//            mExInputPin?.setText("")
//        }
        inText = removeLastDigit(inText)
        mExInputPin?.setText(inText)
        onClickedSubmit(value = inText)
    }

    fun removeLastDigit(currentValue: String): String {
        // 1. Remove all non-digit characters (commas, dots, etc)
        val cleanString = currentValue.replace(Regex("[^\\d]"), "")

        // 2. If it's empty or only 1 digit remains, return the default 0.00
        if (cleanString.length <= 1) {
            return "0.00"
        }

        // 3. Remove the last character
        val newString = cleanString.substring(0, cleanString.length - 1)

        // 4. Convert to Double and reformat
        val parsed = newString.toDouble()
        return NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(parsed / 100)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun onClickedSubmit(value: String, isTakeAction: Boolean = false ) {
//        if (value == null || value.length < 4) {
//            //Toast.makeText(context, "Enter correct PIN", Toast.LENGTH_SHORT).show()
//            return
//        }
        // dismiss()
        mListener?.onBottomSheetItemClick(value,isTakeAction)
    }


    interface ItemClickListener {
        fun onBottomSheetItemClick(clickOption: String?, isTakeAction: Boolean = false)
    }

    companion object {
        // const val TAG = "ActionBottomDialog"
        var mListener: ItemClickListener? = null
        var mIsNumberShuffle = false
        var mIsReadyForPin = false

        fun newInstance(mListener: ItemClickListener, isNumberShuffle: Boolean = false, isReadyForPin: Boolean = false): NumberPadBottomSheet {
            Companion.mListener = mListener
            Companion.mIsNumberShuffle = isNumberShuffle
            Companion.mIsReadyForPin  = isReadyForPin
            return NumberPadBottomSheet()
        }
    }


}