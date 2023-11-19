
class ShowTheBillOnBigScreen : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =  inflater.inflate(R.layout.show_the_bill_on_big_screen_fragment, container, false)
        var imageofBill = arguments?.getString("imageofbill")
        var showtheBill = view.findViewById<ImageView>(R.id.imageofBill_showthebill)

        Glide.with(requireContext()).load(imageofBill).into(showtheBill)

        return view
    }
}
