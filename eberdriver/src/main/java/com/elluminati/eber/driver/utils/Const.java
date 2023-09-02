package com.elluminati.eber.driver.utils;

import static com.elluminati.eber.driver.BuildConfig.BASE_URL;

/**
 * Created by elluminati on 29-03-2016.
 */
public class Const {

    /**
     * Image server URL
     */

    public static String IMAGE_BASE_URL = BASE_URL;

    /**
     * policy and terms
     */
    public static final String TERMS_CONDITIONS = BASE_URL + "get_provider_terms_and_condition";
    public static final String POLICY = BASE_URL + "get_provider_privacy_policy";
    /***
     * Google url
     */
    public static final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/";

    /**
     * Credential
     */
    public static final String DISTANCE_MATRIX_API_BASE = "https://maps.googleapis" +
            ".com/maps/api/distancematrix/json?";
    public static final String DIRECTION_API_BASE = "https://maps.googleapis" +
            ".com/maps/api/directions/json?";
    public static final String GEOCODE_API = "https://maps.googleapis" +
            ".com/maps/api/geocode/json?address=";
    /**
     * Localization
     */

    public static final String EN = "en";

    /**
     * location displacement in meter
     */

    public static final float DISPLACEMENT = 5; //meter


    /**
     * speed measure
     */
    public static final float KM_COEFFICIENT = 3.6f; /// meter/second to km/h


    /**
     * Default font scale for used when app font scale change
     */

    public static final float DEFAULT_FONT_SCALE = 1.0f;
    /**
     * value decide to determine update location
     */
    public static final int DETERMINE_TIME_COUNT_ONE = 120;//10 minuet
    public static final int DETERMINE_TIME_COUNT_TWO = 180;// 15 minuet;
    //CALCULATE_TIME_COUNT=(your set time in minuet )*60/SCHEDULED_SECONDS
    // you can calculate time count as above equation put your "minuet" in equation and get count
    //set calculate time count in your determine time count
    public static final int THRESHOLD_METER = 10;//miter
    /**
     * Pickup Alert sound distance
     */
    public static final int PICKUP_THRESHOLD = 300;//miter
    /**
     * Timer Scheduled in Second
     */
    public static final long SCHEDULED_SECONDS = 5;//seconds
    /**
     * Timer Scheduled in Second for heat map
     */
    public static final long HEAT_MAP_SCHEDULED_SECOND = 30; //seconds
    /**
     * set LatLngBounce padding
     */
    public static final int MAP_BOUNDS = 180;
    /**
     * path draw on or off
     */
    public static final boolean IS_PATH_DRAW = false;
    /**
     * general const
     */
    public static final int SHOW_BOTH_ADDRESS = 0;
    public static final int SHOW_PICK_UP_ADDRESS = 1;
    public static final int SHOW_DESTINATION_ADDRESS = 2;
    public static final String HTTP_ERROR_CODE_PREFIX = "http_error_";
    public static final int PUSH_NOTIFICATION_ID = 2688;
    public static final int FOREGROUND_NOTIFICATION_ID = 2687;
    public static final String IS_HAVE_TRIP = "is_have_trip";
    public static final int PROVIDER_UNIQUE_NUMBER = 11;
    public static final int USER_UNIQUE_NUMBER = 10;
    public static final int ERROR_CODE_YOUR_TRIP_PAYMENT_IS_PENDING = 464;
    public static final int BANK_HOLDER_TYPE = 11;
    public static final String UNIT_PREFIX = "unit_code_";
    public static final int TRUE = 1;
    public static final int FALSE = 0;
    public static final int SERVICE_NOTIFICATION_ID = 2017;
    public static final int PROVIDER = 0;
    public static final int ERROR_CODE_INVALID_TOKEN = 451;
    public static final int CASH = 1;
    public static final int CARD = 0;
    public static final String SOCIAL_FACEBOOK = "facebook";
    public static final String SOCIAL_GOOGLE = "google";
    public static final String MANUAL = "manual";
    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String NAME = "name";
    public static final String COUNTRY_CODE = "country-code";
    public static final String PHONE_CODE = "phone-code";
    public static final String ALPHA2 = "alpha-2";
    public static final String IS_CLICK_INSIDE_DRAWER = "is_click_inside_drawer";
    public static final String ERROR_CODE_PREFIX = "error_code_";
    public static final String MESSAGE_CODE_PREFIX = "message_code_";
    public static final String PUSH_MESSAGE_PREFIX = "push_message_";
    public static final String DATE_TIME_FORMAT_WEB = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_MONTH = "MMMM yyyy";
    public static final String DATE_FORMAT_EARNING = "dd MMM yyyy";
    public static final String DAY = "d";
    public static final String TIME_FORMAT_AM = "h:mm a";
    public static final String UTF_8 = "utf-8";
    public static final String SLASH = "/";
    public static final String PERCENTAGE = "%";
    public static final String EQUAL = "=";
    public static final String AND = "&";
    public static final String COMA = ",";
    public static final String STRING = "string";
    public static final int ACTION_LOCATION_SOURCE_SETTINGS = 1;
    public static final int ACTION_SETTINGS = 2;
    public static final String PIC_URI = "picUri";
    public static final String XIAOMI = "Xiaomi";
    public static final int LOCATION_SETTING_REQUEST = 1080;
    public static final int CODE_USER_CANCEL_TRIP = 807;
    public static final String AMP_KEY = "&key=";
    public static final String ADD_VEHICLE = "add_vehicle";
    public static final String IS_ADD_VEHICLE = "is_add_vehicle";
    public static final String VEHICLE_ID = "vehicle_id";
    public static final int ERROR_PROVIDER_DETAIL_NOT_FOUND = 479;
    public static final String BUNDLE = "BUNDLE";
    public static final int PICK_UP_ADDRESS = 1;
    public static final int DESTINATION_ADDRESS = 2;
    /**
     * Permission requestCode
     */
    public static final int REQUEST_ADD_CARD = 33;
    public static final int REQUEST_ADD_VEHICLE = 37;
    public static final int PERMISSION_FOR_LOCATION = 2;
    public static final int PERMISSION_FOR_CAMERA_AND_EXTERNAL_STORAGE = 3;
    public static final int PERMISSION_FOR_CALL = 4;
    public static final int PERMISSION_FOR_SMS = 6;
    /**
     * Broadcast Action
     */
    public static final String NETWORK_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String GPS_ACTION = "android.location.PROVIDERS_CHANGED";
    public static final String ACTION_DECLINE_PROVIDER = "eber.provider.PROVIDER_DECLINE";
    public static final String ACTION_APPROVED_PROVIDER = "eber.provider.PROVIDER_APPROVED";

    public static final String PROFILE_APPROVED_ACTION = "eber.provider.PROFILE_APPROVED";
    public static final String VIHICLE_APPROVED_ACTION = "eber.provider.VIHICLE_APPROVED";
    public static final String DOCUMENT_APPROVED_ACTION = "eber.provider.DOCUMENT_APPROVED";


    public static final String ACTION_NEW_TRIP = "eber.provider" +
            ".PROVIDER_HAVE_NEW_TRIP";
    public static final String ACTION_ACCEPT_NOTIFICATION = "eber.provider" +
            ".ACCEPT_NOTIFICATION";
    public static final String ACTION_CANCEL_NOTIFICATION = "eber.provider" +
            ".CANCEL_NOTIFICATION";
    public static final String ACTION_CANCEL_TRIP = "eber.provider" +
            ".USER_CANCEL_TRIP";
    public static final String ACTION_DESTINATION_UPDATE = "eber.provider" +
            ".USER_DESTINATION_UPDATE";
    public static final String ACTION_PAYMENT_CASH = "eber.provider.PAYMENT_CASH";
    public static final String ACTION_PAYMENT_CARD = "eber.provider.PAYMENT_CARD";
    public static final String ACTION_PROVIDER_TRIP_END = "eber.provider.PROVIDER_TRIP_END";
    public static final String ACTION_OTP_SMS = "android.provider.Telephony.SMS_RECEIVED";
    public static final String ACTION_TRIP_ACCEPTED_BY_ANOTHER_PROVIDER = "eber.provider" +
            ".TRIP_ACCEPTED_BY_ANOTHER_PROVIDER";
    public static final String ACTION_PROVIDER_OFFLINE = "android.provider.Telephony.ACTION_PROVIDER_OFFLINE";

    /**
     * service parameters
     */

    public class Params {
        public static final String APP_NAME = "application_name";

        public static final String ISENDOFTRIP = "is_end_of_trip";
        public static final String ENDADDRESS = "end_of_trip_address";
        public static final String ENDLAT = "end_of_trip_lat";
        public static final String ENDLNG = "end_of_trip_long";

        public static final String PROVIDER_TYPE_ID = "provider_type_id";

        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String PHONE = "phone";
        public static final String DEVICE_TOKEN = "device_token";
        public static final String DEVICE_TYPE = "device_type";
        public static final String BIO = "bio";
        public static final String TYPE = "type";
        public static final String ADDRESS = "address";
        public static final String COUNTRY = "country";
        public static final String COUNTRY_ID = "country_id";
        public static final String ZIPCODE = "zipcode";
        public static final String LOGIN_BY = "login_by";
        public static final String SOCIAL_UNIQUE_ID = "social_unique_id";
        public static final String COUNTRY_PHONE_CODE = "country_phone_code";
        public static final String CITY = "city";
        public static final String CITY_ID = "city_id";
        public static final String DEVICE_TIMEZONE = "device_timezone";
        public static final String PROVIDER_ID = "provider_id";
        public static final String USER_ID = "user_id";
        public static final String TOKEN = "token";
        public static final String SUCCESS = "success";
        public static final String MESSAGE = "message";
        public static final String ERROR_CODE = "error_code";
        public static final String SERVICE_TYPE = "service_type";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String SUB_ADMIN_CITY = "subAdminCity";
        public static final String TRIP_ID = "trip_id";
        public static final String SOURCE_ADDRESS = "source_address";
        public static final String DESTINATION_ADDRESS = "destination_address";
        public static final String NEW_PASSWORD = "new_password";
        public static final String OLD_PASSWORD = "old_password";
        public static final String IS_PROVIDER_ACCEPTED = "is_provider_accepted";
        public static final String IS_PROVIDER_STATUS = "is_provider_status";
        public static final String IS_ACTIVE = "is_active";
        public static final String TIME = "time";
        public static final String REVIEW = "review";
        public static final String RATING = "rating";
        public static final String PAYMENT_MODE = "payment_mode";
        public static final String UNIT = "unit";
        public static final String CURRENCY = "currency";
        public static final String CANCEL_REASON = "cancel_reason";
        public static final String TYPE_ID = "typeid";
        public static final String PICTURE_DATA = "pictureData";
        public static final String BEARING = "bearing";
        public static final String SOURCE_LOCATION = "sourceLocation";
        ;
        public static final String GOOGLE_PATH_START_LOCATION_TO_PICKUP_LOCATION =
                "googlePathStartLocationToPickUpLocation";
        public static final String
                GOOGLE_PICKUP_LOCATION_TO_DESTINATION_LOCATION
                = "googlePickUpLocationToDestinationLocation";
        public static final String BANK_ACCOUNT_NUMBER = "account_number";
        public static final String BANK_ACCOUNT_HOLDER_NAME = "account_holder_name";
        public static final String BANK_PERSONAL_ID_NUMBER = "personal_id_number";
        public static final String DOB = "dob";
        public static final String BANK_ROUTING_NUMBER = "routing_number";
        public static final String BANK_ACCOUNT_HOLDER_TYPE = "account_holder_type";

        public static final String UNIQUE_CODE = "unique_code";
        public static final String EXPIRED_DATE = "expired_date";
        public static final String DOCUMENT_ID = "document_id";
        public static final String DOCUMENT = "document";
        public static final String TOLL_AMOUNT = "toll_amount";
        public static final String TIP_AMOUNT = "tip_amount";
        public static final String APP_VERSION = "app_version";
        public static final String PICKUP_LOCATIONS = "pickup_locations";
        public static final String TIMEZONE = "timezone";
        public static final String DATE = "date";
        public static final String PICK_UP_LATITUDE = "latitude";
        public static final String PICK_UP_LONGITUDE = "longitude";
        public static final String DEST_LATITUDE = "d_latitude";
        public static final String DEST_LONGITUDE = "d_longitude";
        public static final String IS_SURGE_HOURS = "is_surge_hours";
        public static final String SERVICE_TYPE_ID = "service_type_id";
        public static final String DISTANCE = "distance";
        public static final String PICKUP_LAT = "pickup_latitude";
        public static final String PICKUP_LON = "pickup_longitude";
        public static final String DEST_LAT = "destination_latitude";
        public static final String DEST_LON = "destination_longitude";
        public static final String VEHICLE_NAME = "vehicle_name";
        public static final String PLATE_NO = "plate_no";
        public static final String MODEL = "model";
        public static final String COLOR = "color";
        public static final String PASSING_YEAR = "passing_year";
        public static final String VEHICLE_ID = "vehicle_id";
        public static final String PAYMENT_TOKEN = "payment_token";
        public static final String PAYMENT_METHOD = "payment_method";
        public static final String LAST_FOUR = "last_four";
        public static final String CARD_TYPE = "card_type";
        public static final String WALLET = "wallet";
        public static final String CARD_ID = "card_id";
        public static final String ACCESSIBILITY = "accessibility";
        public static final String LANGUAGES = "languages";
        public static final String LOCATION_UNIQUE_ID = "location_unique_id";
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
        public static final String SURGE_MULTIPLIER = "surge_multiplier";
        public static final String IS_REQUEST_TIMEOUT = "is_request_timeout";
        public static final String REFERRAL_CODE = "referral_code";
        public static final String IS_SKIP = "is_skip";
        public static final String STATE = "state";
        public static final String POSTAL_CODE = "postal_code";
        public static final String GENDER = "gender";
        public static final String PCO_LICENCE = "pco_licence";

    }

    /**
     * app request code
     */

    public class ServiceCode {

        public static final int CHOOSE_PHOTO = 4;
        public static final int TAKE_PHOTO = 5;
        public static final int PATH_DRAW = 7;
        public static final int GET_GOOGLE_MAP_PATH = 39;
    }

    /**
     * provider status
     */

    public class ProviderStatus {
        public static final int IS_REFERRAL_SKIP = 1;
        public static final int IS_REFERRAL_APPLY = 0;
        public static final int PROVIDER_STATUS_ONLINE = 1;
        public static final int PROVIDER_STATUS_OFFLINE = 0;
        public static final int PROVIDER_STATUS_TRIP_CANCELLED = 1;
        public static final int PROVIDER_STATUS_ACCEPTED_PENDING = 2;
        public static final int PROVIDER_STATUS_ACCEPTED = 1;
        public static final int PROVIDER_STATUS_REJECTED = 0;
        public static final int PROVIDER_STATUS_IDEAL = 0;
        public static final int PROVIDER_STATUS_STARTED = 2;
        public static final int PROVIDER_STATUS_ARRIVED = 4;
        public static final int PROVIDER_STATUS_TRIP_STARTED = 6;
        public static final int PROVIDER_STATUS_TRIP_END = 9;
        public static final int IS_UPLOADED = 1;
        public static final int IS_APPROVED = 1;
        public static final int IS_DECLINED = 0;
        public static final int PROVIDER_TYPE_PARTNER = 1;
        public static final int IS_DEFAULT = 1;

    }

    /**
     * all activity and fragment TAG for log
     */
    public class Tag {
        public static final String FCM_MESSAGING_SERVICE = "FcmMessagingService";
        public static final String MAIN_ACTIVITY = "MainActivity";
        public static final String MAIN_DRAWER_ACTIVITY = "MainDrawerActivity";
        public static final String REGISTER_ACTIVITY = "RegisterActivity";
        public static final String SIGN_IN_ACTIVITY = "SignInActivity";
        public static final String DOCUMENT_ACTIVITY = "DocumentActivity";
        public static final String MAP_FRAGMENT = "MapFragment";
        public static final String PATH_DRAW_ON_MAP = " PathDrawOnMap";
        public static final String TRIP_FRAGMENT = "trip_fragment";
        public static final String TRIP_HISTORY_ACTIVITY = "TripHistoryActivity";
        public static final String FEEDBACK_FRAGMENT = "FeedbackFragment";
        public static final String PROFILE_ACTIVITY = "ProfileActivity";
        public static final String INVOICE_FRAGMENT = "invoice_fragment";
        public static final String TRIP_HISTORY_DETAIL_ACTIVITY = "TripHistoryDetailActivity";
        public static final String BANK_DETAIL_ACTIVITY = "BankDetailActivity";
        public static final String VIEW_AND_ADD_PAYMENT_ACTIVITY = "ViewPaymentActivity";
    }

    /**
     * params for google
     */
    public class google {
        public static final String LAT_LNG = "latlng";
        public static final String ERROR_MESSAGE = "error_message";
        public static final String FORMATTED_ADDRESS = "formatted_address";
        public static final String DESTINATION_ADDRESSES = "destination_addresses";
        public static final String ROWS = "rows";
        public static final String ELEMENTS = "elements";
        public static final String DISTANCE = "distance";
        public static final String VALUE = "value";
        public static final String DURATION = "duration";
        public static final String ROUTES = "routes";
        public static final String LEGS = "legs";
        public static final String STEPS = "steps";
        public static final String POLYLINE = "polyline";
        public static final String POINTS = "points";
        public static final String LAT = "lat";
        public static final String LNG = "lng";
        public static final String ORIGIN = "origin";
        public static final String ORIGINS = "origins";
        public static final String DESTINATION = "destination";
        public static final String DESTINATIONS = "destinations";
        public static final String KEY = "key";
        public static final String EMAIL = "email";
        public static final String ID = "id";
        public static final String PICTURE = "picture";
        public static final String URL = "url";
        public static final String DATA = "data";
        public static final String NAME = "name";
        public static final String FIELDS = "fields";
        public static final int RC_SIGN_IN = 2001;
        public static final String OK = "OK";
        public static final String ADDRESS_COMPONENTS = "address_components";
        public static final String TYPES = "types";
        public static final String LOCALITY = "locality";
        public static final String LONG_NAME = "long_name";
        public static final String ADMINISTRATIVE_AREA_LEVEL_2 = "administrative_area_level_2";
        public static final String ADMINISTRATIVE_AREA_LEVEL_1 = "administrative_area_level_1";
        public static final String COUNTRY = "country";
        public static final String RESULTS = "results";
        public static final String GEOMETRY = "geometry";
        public static final String LOCATION = "location";
        public static final String STATUS = "status";
        public static final String COUNTRY_CODE = "country_code";
        public static final String SHORT_NAME = "short_name";
    }

    public class TripType {
        public static final int AIRPORT = 11;
        public static final int ZONE = 12;
        public static final int CITY = 13;
        public static final int NORMAL = 0;
        public static final int SCHEDULE_TRIP = 5;
        public static final int HOTEL_PICKUP = 2;
        public static final int TRIP_TYPE_CAR_RENTAL = 14;
        public static final int TRIP_TYPE_CORPORATE = 7;
    }


    public class Wallet {
        public static final int ADD_WALLET_AMOUNT = 1;
        public static final int REMOVE_WALLET_AMOUNT = 2;

        public static final int ADDED_BY_ADMIN = 1;
        public static final int ADDED_BY_CARD = 2;
        public static final int ADDED_BY_REFERRAL = 3;
        public static final int ORDER_PROFIT = 6;
    }

    public class Accessibility {
        public static final String HANDICAP = "handicap";
        public static final String BABY_SEAT = "baby_seat";
        public static final String HOTSPOT = "hotspot";
    }

    public class Gender {
        public static final String MALE = "male";
        public static final String FEMALE = "female";
    }

    public class Bank {
        public static final String BANK_ACCOUNT_HOLDER_TYPE = "individual";
    }

    public class Action {
        public static final String STARTFOREGROUND_ACTION = "com.eber.action" +
                ".startforeground";
        public static final String STOPFOREGROUND_ACTION = "com.eber.action" +
                ".stopforeground";
    }

    public class Pending {
        public static final int PENDING_FOR_ADMIN_APPROVAL = 0;
        public static final int ADD_VEHICLE = 1;
        public static final int DOCUMENT_EXPIRE = 3;
    }

}
