package com.example.taskscontinuity

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays

class ArticleInfo(val id: Int, val position: Float)

class Article(
  val id: Int,
  val title: String,
  val author: String,
  val date: String,
  val image: Int,
  val description: String,
)

class NewsViewModel : NearByCallbacks, ViewModel() {


  val articles = arrayListOf<Article>(
    Article(
      1,
      "Owning a Bay Area home now twice as expensive as renting — should that ‘tilt the needle’?",
      "Danielle Echeverria",
      "June 9, 2023",
      R.drawable.one,
      "Living in the Bay Area is expensive across the board, but the monthly cost of homeownership is now more than twice as expensive as monthly rent for the same home, according to a report from real estate company Redfin — the largest such disparity anywhere in the country.\n" +
              "\n" +
              "Experts say the disparity is so extreme that for people weighing their options, renting in the Bay Area might be uniquely more attractive than buying compared to other places in the U.S.\n" +
              "\n" +
              "The Redfin analysis, which looked at single-family homes, condominiums and townhouses, used the company’s estimate of home values and a 6.5% mortgage rate (the average rate in March this year) to estimate monthly payments, assuming a 5% down payment, homeowner’s insurance rate equal to 0.5% and factoring in property tax rates for each property. It then compared those numbers with Redfin rental estimates. "
              + "\n" +
              "The researchers found that, in San Jose, the typical home is 165% more expensive per month to buy than to rent — the highest “homeownership premium” of any of the 50 most populous metro areas in the country. San Francisco followed at 139% and Oakland at 99%. In each Bay Area metro, 0% of homes were more expensive to rent than to buy."
              + "\n" +
              "While the Bay Area saw the highest homeownership premiums, the disparity exists in all but four of the 50 largest metropolitan areas in the U.S. — Detroit, Philadelphia, Cleveland and Houston. Nationwide, the typical home costs 25% more to buy than to rent, the report found."
              + "\n" +
              "According to the National Multifamily Housing Council, the monthly premium to buy vs. rent, or the disparity between the cost of buying and the cost of renting, is at a historic high due to a combination of rapid home price appreciation and rising interest rates.\n" +
              "\n" +
              "The findings did not surprise real estate economists who did not work on the Redfin report. \n" +
              "\n" +
              "“It strikes me as something that fits with my understanding of the market,” said Christopher Salviati, a senior economist at rental listing site Apartment List. “The fact that we’re seeing that disparity in some sense is expected, but the magnitude here in the Bay Area is obviously more extreme than it is in the rest of the country.”\n" +
              "\n" +
              "Jeff Tucker, a senior economist at real estate listings website Zillow, agreed. He plugged Zillow’s most recent home value index for the San Francisco metro area into a mortgage calculator and found that the monthly payment was about \$5,850. The area’s typical rent, as estimated by Zillow, was about \$3,150."
              + "\n" +
              "“San Jose and San Francisco are the No. 1 and No. 2 most expensive places to rent in the country. But \$3,000 for rent suddenly seems like kind of a bargain compared to five, six, \$7,000 on a mortgage,” he said. “The cost of homeownership there is just mind-boggling, really.”\n"
              + "\n" +
              "For those lucky enough to be able to consider buying the Bay Area, the disparity between renting and buying changes the calculus a bit, the economists said. Homeownership carries definite benefits, including wealth-building opportunities, a fixed monthly cost, tax benefits and the freedom to truly personalize your home. But the decision is very individual, they noted.\n" +
              "\n" +
              "“Buying a home often makes more financial sense than renting if you can afford a down payment and monthly mortgage because you’re building equity,” Marr wrote in the report. “When you own your home, your home pays you; when you rent, you and your home pay your landlord.” \n" +
              "\n" +
              "But for some others — including people who move around a lot and won’t stay long enough to build that equity, for example — buying makes less sense, he said." +
              "\n" +
              "Salviati said that because the gap between the cost of renting and buying is so large in the Bay Area, a person’s reasons for owning would need to be a lot stronger than they might be in other markets.\n" +
              "\n" +
              "“It certainly tilts the needle towards renting,” he said.\n" +
              "\n" +
              "Tucker added that people who opt to rent the type of home they could afford rather than buy might prefer to invest that money elsewhere.\n" +
              "\n" +
              "“Out of anywhere in the country, I do think there’s a pretty strong argument to just rent your home and invest all that money it would have taken to buy, especially given how high prices and mortgage rates have gotten in the Bay Area,” he said.\n"
              + "\n" +
              "Several listings on Zillow illustrate the trend — a \$1.15 million home in San Francisco’s NoPa neighborhood, for example, has an estimated monthly payment of \$7,488, based on a 20% down payment, a 6.8% interest rate and factoring in expenses like property taxes and home insurance — even more difficult now as large insurance companies like State Farm and Allstate have stopped writing new homeowner policies in California. The estimated rental cost for the same property is \$4,800. \n" +
              "\n" +
              "A \$759,000 apartment in SoMa had a monthly estimated buyer cost of \$5,635, compared with a rental estimate of \$3,240. On the other end of the price spectrum, a \$3.5 million home in the Sunset comes with a \$22,770 estimated monthly cost, with an \$11,647-per-month rent estimate."
              + "\n" +
              "Several factors play into the price disparity, economists said. In the report, Redfin economist Taylor Marr pointed to the rise in mortgage rates as a significant factor. In the Bay Area, where prices are already so high, rate increases can translate into payments that are thousands of dollars higher, he noted.\n" +
              "\n" +
              "Tucker added that the extra costs of homeownership — maintenance, insurance and property taxes — are also quite high for new home buyers in the Bay Area, pushing the cost of ownership up even more.\n" +
              "\n" +
              "Salviati added that the Bay Area’s very short supply of housing and low inventory of homes on the market means that home prices remain stubbornly high, even when they are down compared to before the pandemic.\n" +
              "\n" +
              "“This is a place where a lot of people want to live, and we haven’t been building enough housing to meet that demand. This is a shortage that’s accrued over the course of decades,” he said. “I don’t think it’s likely that we’re going to see the affordability situation here improve anytime soon.”"

    ),
    Article(
      2,
      "SFChronicle names new lead restaurant critic",
      "Janelle Bitker",
      "June 9, 2023",
      R.drawable.two,
      "From Senior Food & Wine Editor Janelle Bitker:\n" +
              "\n" +
              "The San Francisco Chronicle has named MacKenzie Chung Fegan to be the newspaper’s next lead restaurant critic.\n" +
              "\n" +
              "Currently a senior editor at Bon Appetit magazine, Bay Area native Fegan has written about restaurants, worked in restaurants and even grew up in a restaurant family. In 1974, her grandparents opened locally beloved Henry’s Hunan in San Francisco, which the New Yorker once called “the best Chinese restaurant in the world.” She understands the weight and importance of restaurant criticism on a deeply personal level."
              + "\n" +
              "“MacKenzie will follow in the footsteps of some of the best restaurant critics in history, continuing the tradition of Michael Bauer and Soleil Ho and adding her own distinctive voice to the job,” said Chronicle Editor in Chief Emilio Garcia-Ruiz. “The restaurant scene in San Francisco is second to none, and our readers demand a smart, discerning voice to help guide their choices.”\n" +
              "\n" +
              "Fifth & Mission podcast: How Henry’s Hunan Shaped The Chronicle’s New Restaurant Critic\n" +
              "Fegan’s work has appeared in national outlets such as GQ, Playboy and Vice, as well as The San Francisco Chronicle. Her profile of chef Brandon Jew of Mister Jiu’s for Resy appeared in the 2021 edition of “The Best American Food Writing.” She’s also a multimedia force, having worked as a TV and podcast host for BRIC TV in New York, and directed and produced documentaries that have won Webby Awards.\n" +
              "\n" +
              "Fegan succeeds Ho, who transitioned earlier this year to a columnist role on the Opinion Desk after four years with the Food & Wine team. Fegan will help diners find amazing food all over the region through Top Restaurants guides, reviews and features alongside associate restaurant critic Cesar Hernandez. Fegan will start in January, after taking maternity leave."
              + "\n" +
              "“I can’t imagine a better reason to return to the Bay Area than the opportunity to join The Chronicle’s incredible food team,” Fegan said. “Soleil Ho’s thoughtful commitment to examining food through the lenses of broader cultural forces changed the face of restaurant criticism — not just on a local or regional level, but on a national scale. I hope to continue that work.”"

    ),
    Article(
      3,
      "Don’t have a California park reservation yet? Here’s how to snag a last-minute campsite",
      "Kellie Hwang",
      "June 9, 2023",
      R.drawable.three,
      "California’s parks are a huge draw for vacationers, so it should be no surprise that many popular campsites across the state were booked for this summer well in advance. \n" +
              "\n" +
              "The two main campground reservation systems — ReserveCalifornia, which manages bookings for state parks, and Recreation.gov, which handles national park reservations — open reservations for most campsites six months in advance, making the competition especially cutthroat.\n" +
              "\n" +
              "A bill that recently passed the California Assembly would make booking campsites at state parks easier. AB 618 would allow those who cancel a reservation within seven days of the start date to receive credit for a future reservation if used within five years. It would also require the state parks department to implement a lottery system for up to five of the most popular campsites."
              + "\n" +
              "“California’s state parks are loved by many within and outside California,” said Adeline Yee, spokesperson for California State Parks, which manages the ReserveCalifornia system. “It is very common for the demand for camping and lodging sites to exceed our inventory. This summer is not an exception. The majority of campground sites have been reserved for the summer.”\n" +
              "\n" +
              "Still, with 13,000 campsites on ReserveCalifonia, there are bound to be openings for those who take the time to search around, particularly in less popular destinations or walk-up or dispersed campgrounds. And there are many private campgrounds across the state, too. \n" +
              "\n" +
              "Here are some tips for how to score a last-minute campsite in California this summer."
              + "\n" +
              "Investigate your options closely online. What area do you want to visit? Do you have specific needs (pet accommodations, shade, near water, flushing toilets, ADA accessible, etc.)? Are you tent camping or traveling with a trailer or in an RV?\n" +
              "\n" +
              "When choosing your dates, have backup dates or a wide date range in mind. And be sure to pre-register on the booking websites, too. Use the ReserveCalifornia search tool to help narrow it down."

              + "\n" +
              "ReserveCalifornia sites open for bookings six months in advance and remain open until two days prior to an arrival date. Most campgrounds for Recreation.gov open for booking at 7 a.m. PST six months prior to arrival. Some campgrounds may have different rules, so check the “Seasons and Fees” table for specific times and dates."
              + "\n" +
              "Cancellations can be made at least a day before to avoid penalties on both sites. For ReserveCalifornia, cancellations after 6 p.m. the day before an  arrival date are subject to a \$7.99 fee, while most facilities don’t allow cancellations after midnight local time on the reservation check-in date. \n" +
              "\n" +
              "So if you’re flexible, check the sites regularly for last-minute cancellations. ReserveCalifornia says people can also reach its call center at (800) 444-PARK (7275), and while cancellations are rare, they can occur.\n" +
              "\n" +
              "Be flexible\n" +
              "If a specific campground is full, both ReserveAmerica and Recreation.gov show campsites nearby to consider. Recreation.gov allows users to sort availability by a “flexible” option and has a “next available” button to skip to the next open date. \n" +
              "\n" +
              "ReserveCalifornia has a notification alert feature if a desired campground is unavailable. Choose the date range, facility and contact method, and cross your fingers.\n" +
              "\n" +
              "Holiday weekends can be especially difficult, and weekend reservations are also popular. But for those able to go during the week, they might have better luck (plus, some campgrounds might be less crowded). And consider the “shoulder season” of April, May, September and October, when parks are less crowded and the weather is still pleasant.\n" +
              "\n" +
              "Look for less popular campgrounds. National parks are likely to book up quickly, as are Southern California coastal campgrounds that Yee says “sell out within minutes of inventory becoming available.” \n" +
              "\n" +
              "Yee said some of the most popular state parks include Crystal Cove State Park in Orange County, which has only 24 sites available, Steep Ravine cabins at Mount Tamalpais State Park, and the campgrounds at Samuel P. Taylor State Park in Marin County. \n" +
              "\n" +
              "ReserveCalifornia suggests looking at state parks north of Santa Cruz or farther inland, and also more remote sites away from metro areas. "
              + "\n" +
              "Look for walk-up campsites\n" +
              "California State Parks has 20 campgrounds that offer first-come, first-served campsites including four in the Bay Area. The website notes that the sites usually fill up by early Friday morning on holiday weekends, so show up early if you want to take a gamble on a busy weekend, and have back-up plans. Some campsites on Recreation.gov are labeled “NR,” indicating they are not available to book until the date of arrival. \n" +
              "\n" +
              "Have an RV or trailer? Check out self-contained camping vehicle campsites. These are normally parking lots during the day, so campers need to be out by 9 a.m. the next day. Just call ahead to ensure your vehicle can be accommodated.\n" +
              "\n" +
              "Try private campgrounds\n" +
              "There are many camping, glamping and RV sites across the state that are on private land "
              + "\n" +
              "Be ready for next season \n" +
              "For those who had their heart set on a campground or area that’s already booked, prepare for next year. January is one of the most popular times to make upcoming campground reservations, so mark your calendar and set an alarm for those on-sale dates.\n" +
              "\n" +
              "According to Active Norcal, campers can log on to ReserveCalifornia 20 minutes prior to the on sale date, plug in the campground name, and once the reservation window opens, all sites will be open. "

    ),

    Article(
      4,
      "BART’s new budget: Fare and parking increases with a \$93 million deficit looming",
      "Ricardo Cano",
      "June 9, 2023",
      R.drawable.four,
      "BART’s Board of Directors approved 11% fare hikes, raised parking rates and passed a two-year budget that contains a \$93 million deficit starting in 2025.\n" +
              "\n" +
              "Thursday’s decisions by the transit agency’s board come on the eve of Sacramento lawmakers’ deadline to pass a state budget that BART and other Bay Area agencies hope will include a \$5 billion bailout.\n" +
              "\n" +
              "Transit agencies want Gov. Gavin Newsom and state legislators to pass a subsidy to maintain service after they run out of the \$4.5 billion in federal aid that kept transit running during the pandemic."

              + "\n" +
              "The situation is especially perilous for BART. Fares covered 70% of the agency’s operating costs before the pandemic. But ridership has remained stagnant since last fall — at about 40% of 2019 figures — as remote work and riders’ concerns about crime, homelessness and cleanliness on BART hobble its recovery.\n" +
              "\n" +
              "BART leaders say the agency won’t survive without state assistance and, eventually, a tax increase from local voters to subsidize service."
              + "\n" +
              "Here’s what we know about the budget decision.\n" +
              "\n" +
              "BART budget keeps a \$93 million deficit\n" +
              "The most tense portion of Thursday’s meeting came during board directors’ budget vote.\n" +
              "\n" +
              "The budget approved by the agency’s progressive majority on a 6-3 vote includes a \$93 million deficit in fiscal 2025. BART will reach its “fiscal cliff” in March of that year when it runs out of federal funds."
              + "\n" +
              "That deficit will grow to \$342 million in fiscal 2026 and account for 30% of BART’s \$1.1 billion operating budget because it will be the first year that BART won’t have any federal aid to cover expenses. By 2028, BART projects to have a cumulative deficit of \$1 billion.\n" +
              "\n" +
              "During this same span, BART projects its operating expenses will go up by 17% — from \$1.1 billion in 2024 to \$1.27 billion in 2028."

              + "\n" +
              "BART’s 2024 budget increases operating costs by 7.5% compared to last year, in part to continue 10.5% across-the-board raises through 2024 that agency officials approved last July, as well as ongoing hiring. That hiring includes four new positions to the agency’s Office of the Inspector General.\n" +
              "\n" +
              "The budget voted by the board’s majority includes schedule changes in September — 20-minute headways on weekdays and weekends — that officials hope will bring a ridership boost.\n" +
              "\n" +
              "Agency officials acknowledged that they will have to consider cutting expenses in the fall if California’s budget doesn’t include a transit bailout. It’s unclear if that would mean reducing service. BART budget director Chris Simi said agency staff will “evaluate a number of budget mitigations” to present to the board in October.\n" +
              "\n" +
              "The agency is waiting to see whether it will get a subsidy in the state budget. However, the Newsom administration has cast doubt about that happening as California faces its own \$32 billion deficit.\n" +
              "\n" +
              "“It’s not an ideal situation we’re in,” board Director Rebecca Saltzman said. “An ideal situation would have been, we’d have a governor like the one in New York who would have stepped up and fixed the situation already.” New York state’s budget included a bailout for New York City transit.\n" +
              "\n" +
              "Board Director Debora Allen, a member of the board’s ideological minority who voted against the budget, said the agency should have cut costs to balance its fiscal 2025 budget.\n" +
              "\n" +
              "Allen said BART’s rising operating costs are not sustainable considering its looming fiscal cliff. Other members of the majority, however, said it would be irresponsible for BART to cut costs ahead of knowing whether it will receive state assistance.\n" +
              "\n" +
              "“We’re saying to the taxpayers, ‘Give us more money now and then we’ll see what we can do later about addressing our spending after we have the money,’ ” Allen said. “I’m sorry, folks, I have zero confidence that that will happen.”"

              + "\n" +
              "Fares and parking rates are going up\n" +
              "The transit board also approved inflationary fare increases that raise fares 11% over two years. They’re expected to bring BART \$26 million in new revenue through 2025.\n" +
              "\n" +
              "Once those increases take full effect in January 2025, it would cost \$5 to take BART from downtown Berkeley to San Francisco’s Embarcadero Station compared to the current rate of \$4.75.\n" +
              "\n" +
              "The board also voted to increase discounts for low-income riders. Fares will be 50% off for riders who earn under 200% of the federal poverty level.\n" +
              "\n" +
              "Parking rates are changing, as well. Under the board-approved proposal, BART will raise parking fees at its lots from \$1-\$3 to \$3-\$6.30. Starting in 2024, BART will enforce parking fees on weekends and weekday afternoons from 3 p.m. to 6 p.m. if station lots are more than 90% full.\n" +
              "\n" +
              "Most station lots are far off from reaching that point, but a majority of board directors supported the changes because parking revenues don’t cover BART’s parking costs.\n" +
              "\n" +
              "“Our riders who don’t park are subsidizing the riders who do park, which doesn’t really make any sense,” Saltzman said.\n" +
              "\n" +
              " The added fare and parking revenue will help narrow BART’s budget gaps, though it won’t be enough to avert service cuts under the agency’s worst-case scenario."
    )


  )


  val toDispatch = MutableSharedFlow<ArticleInfo>()

  val state = mutableStateOf<String>("HELLO")

  val articleId = mutableStateOf(-1)

  var scrollPosition = mutableStateOf(2500f)


  private var isConnected = false

  fun dispatch(data: ArticleInfo) {
    if (isConnected) {
      Log.d("Dispatch", "Emit ${data.id}")
      //toDispatch.tryEmit(data)
      viewModelScope.launch {
        toDispatch.emit(data)
      }
    }
  }

  fun closeArticle() {
    this.articleId.value = -1
    scrollPosition.value = 0f
  }

  fun openArticle(id: Int, position: Float = 0f) {
    Log.d("DataReceived", "openArticle: $id at $position")
    this.articleId.value = id
    scrollPosition.value = position
  }

  fun getArticle(id: Int): Article {
    return articles.find { it.id == id }!!
  }

  fun scrollPosition(position : Float) {
    scrollPosition.value = position
  }


  override fun onAdvertisingStarted() {
    state.value = "Advertising..."
  }

  override fun onAdvertisingFailed() {
    state.value = "Advertising failed"
    super.onAdvertisingFailed()
  }

  override fun onConnectionInitiated(endpoint: Endpoint?, connectionInfo: ConnectionInfo?) {
    super.onConnectionInitiated(endpoint, connectionInfo)
    state.value = "Connection initiated with ${endpoint?.name}"
  }

  override fun onDiscoveryStarted() {
    super.onDiscoveryStarted()
    state.value = "Discovery started"
  }

  override fun onDiscoveryFailed() {
    super.onDiscoveryFailed()
    state.value = "Discovery failed"
  }

  override fun onEndpointDiscovered(endpoint: Endpoint) {
    super.onEndpointDiscovered(endpoint)
    state.value = "Endpoint discovered: ${endpoint.name}"
  }

  override fun onConnectionFailed(endpoint: Endpoint) {
    super.onConnectionFailed(endpoint)
    state.value = "Connection failed: ${endpoint.name}"
  }

  override fun onEndpointConnected(endpoint: Endpoint) {
    super.onEndpointConnected(endpoint)
    state.value = "Endpoint connected! ${endpoint.name}"
    isConnected = true

    if(articleId.value > 0) {
      Log.d("Sending Data", "dispatch: $${articleId.value}, ${scrollPosition.value}")
      dispatch(ArticleInfo(articleId.value, scrollPosition.value))
    }
  }

  override fun onEndpointDisconnected(endpoint: Endpoint) {
    super.onEndpointDisconnected(endpoint)
    state.value = "Endpoint disconnected: ${endpoint.name}"
    isConnected = false
  }

  override fun onReceive(endpoint: Endpoint, payload: Payload?) {
    super.onReceive(endpoint, payload)
    Log.v("DataReceived", "from ${endpoint.name}")

    if (payload?.type == Payload.Type.STREAM) {


    } else if (payload?.type == Payload.Type.BYTES) {
      val data = payload.asBytes()
      if(data?.size == Int.SIZE_BYTES + Float.SIZE_BYTES) {
        val idByte = Arrays.copyOfRange(data, 0, 4)
        val id = ByteBuffer.wrap(idByte).order(ByteOrder.BIG_ENDIAN).getInt()

        val scrollByte = Arrays.copyOfRange(data, 4, 8)
        val scroll = ByteBuffer.wrap(scrollByte).order(ByteOrder.BIG_ENDIAN).getFloat()

        openArticle(id = id, position = scroll)
      }
    }
  }
}