#ProntoApp

Basically, the application (using/requesting) permissions, show you your actual position on the map and you are able to search any type of the businesses nearby you or you can change map position and look on that specific area, showing all those bussinesses with markers and displaying your previous search as a blue circle and a red circle as the actual LatLng to query.

Also, you can see on a Fragment the content as a list. You can click a marker to see it on a list or click the list to see the marker.

Slightly deeper, I am using MVP, MVVM with Dagger2 and Retrofit2.

Starting with the PlacesMapActivity (MVP), you will see all the behaviour of the map. The PlacesRecyclerFragment will display the list with the elements.
