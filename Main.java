import java.util.*;

//---------------- Seat ----------------
class Seat {
    int coachNo, cabinNo, seatNo;
    String berthType; // Upper, Lower, Middle, SideUpper, SideLower
    boolean isOccupied;
    String passengerName;

    Seat(int coachNo, int cabinNo, int seatNo, String berthType) {
        this.coachNo = coachNo;
        this.cabinNo = cabinNo;
        this.seatNo = seatNo;
        this.berthType = berthType;
        this.isOccupied = false;
        this.passengerName = "";
    }
}

//---------------- Passenger ----------------
class Passenger {
    String name, gender, berthPreference;
    int age;
    String allottedBerth, status;
    int coachNo, cabinNo, seatNo;

    Passenger(String name, int age, String gender, String berthPreference) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.berthPreference = berthPreference;
        this.allottedBerth = "NA";
        this.status = "Waiting";
        this.coachNo = this.cabinNo = this.seatNo = -1;
    }
}

//---------------- Booking ----------------
class Booking {
    String pnr;
    List<Passenger> passengers;

    Booking(String pnr) {
        this.pnr = pnr;
        this.passengers = new ArrayList<>();
    }
}

//---------------- Reservation System ----------------
class ReservationSystem {
    private int totalConfirmedSeats = 14;
    private int racLimit = 2;
    private int confirmedCount = 0;
    private int racCount = 0;
    private int pnrCounter = 0;

    List<List<Seat>> coaches; // coaches[coachNo][seatIndex]
    Map<String, Booking> bookings;
    Queue<Passenger> racList;
    Queue<Passenger> waitingList;

    ReservationSystem() {
        coaches = new ArrayList<>();
        bookings = new HashMap<>();
        racList = new LinkedList<>();
        waitingList = new LinkedList<>();
        initializeCoach(); // Start with 1 coach
    }

    // Initialize coach with seats
    private void initializeCoach() {
        List<Seat> coach = new ArrayList<>();

        // Cabin 1: 8 seats
        coach.add(new Seat(1, 1, 1, "Lower"));
        coach.add(new Seat(1, 1, 2, "Middle"));
        coach.add(new Seat(1, 1, 3, "Upper"));
        coach.add(new Seat(1, 1, 4, "Lower"));
        coach.add(new Seat(1, 1, 5, "Middle"));
        coach.add(new Seat(1, 1, 6, "Upper"));
        coach.add(new Seat(1, 1, 7, "SideLower"));
        coach.add(new Seat(1, 1, 8, "SideUpper"));

        // Cabin 2: 8 seats
        coach.add(new Seat(1, 2, 1, "Lower"));
        coach.add(new Seat(1, 2, 2, "Middle"));
        coach.add(new Seat(1, 2, 3, "Upper"));
        coach.add(new Seat(1, 2, 4, "Lower"));
        coach.add(new Seat(1, 2, 5, "Middle"));
        coach.add(new Seat(1, 2, 6, "Upper"));
        coach.add(new Seat(1, 2, 7, "SideLower"));
        coach.add(new Seat(1, 2, 8, "SideUpper"));

        coaches.add(coach);
    }

    private String normalizeBerthPreference(String pref) {
        if (pref == null) return "Lower";

        switch (pref.toUpperCase().charAt(0)) {
            case 'L': return "Lower";
            case 'M': return "Middle";
            case 'U': return "Upper";
            case 'S':
                if (pref.toUpperCase().contains("U")) return "SideUpper";
                else return "SideLower";
            default: return "Lower";
        }
    }

    private String generatePNR() {
        return "PNR" + (++pnrCounter);
    }

    // Book Ticket
    void bookTicket(List<Passenger> passengers) {
        String pnr = generatePNR();
        Booking booking = new Booking(pnr);

        // Try to allocate seats in same coach/nearby for family
        allocateFamily(passengers);

        for (Passenger p : passengers) {
            booking.passengers.add(p);
        }
        bookings.put(pnr, booking);

        System.out.println("🎫 Booking Successful. PNR: " + pnr);
    }

    private void allocateConfirmedSeat(Passenger p) {
        String preferredBerth = p.berthPreference;

        // Senior citizen gets lower berth preference
        if (p.age >= 60) {
            preferredBerth = "Lower";
        }

        // Try to find preferred berth first
        for (int coachIdx = 0; coachIdx < coaches.size(); coachIdx++) {
            List<Seat> coach = coaches.get(coachIdx);
            for (Seat seat : coach) {
                if (!seat.isOccupied && seat.berthType.equals(preferredBerth)) {
                    assignSeat(p, seat);
                    return;
                }
            }
        }

        // If preferred not available, assign any available seat
        for (int coachIdx = 0; coachIdx < coaches.size(); coachIdx++) {
            List<Seat> coach = coaches.get(coachIdx);
            for (Seat seat : coach) {
                if (!seat.isOccupied) {
                    assignSeat(p, seat);
                    return;
                }
            }
        }
    }

    private void assignSeat(Passenger p, Seat seat) {
        seat.isOccupied = true;
        seat.passengerName = p.name;
        p.coachNo = seat.coachNo;
        p.cabinNo = seat.cabinNo;
        p.seatNo = seat.seatNo;
        p.allottedBerth = seat.berthType;
        p.status = "Confirmed";
        confirmedCount++;
    }

    // Allocate seats for family (same coach preference)
    private void allocateFamily(List<Passenger> passengers) {
        for (Passenger p : passengers) {
            if (p.age < 5) {
                p.status = "No Seat (Child < 5)";
                continue;
            }

            if (confirmedCount < totalConfirmedSeats) {
                allocateConfirmedSeat(p);
            } else if (racCount < racLimit) {
                p.status = "RAC";
                racList.add(p);
                racCount++;
            } else {
                p.status = "Waiting";
                waitingList.add(p);
            }
        }
    }




    // Show PNR Details
    void showPNR(String pnr) {
        Booking booking = bookings.get(pnr);
        if (booking == null) {
            System.out.println("❌ Invalid PNR");
            return;
        }

        System.out.println("\n📌 PNR: " + pnr);
        System.out.println("=" + "=".repeat(49));
        for (Passenger p : booking.passengers) {
            System.out.println("👤 Name: " + p.name);
            System.out.println("   Age: " + p.age + " | Gender: " + p.gender);
            System.out.println("   Berth: " + p.allottedBerth + " | Status: " + p.status);
            if (!p.status.equals("No Seat (Child < 5)") && !p.status.equals("Waiting") && !p.status.equals("RAC")) {
                System.out.println("   Coach: " + p.coachNo + " | Cabin: " + p.cabinNo + " | Seat: " + p.seatNo);
            }
            System.out.println();
        }
    }

    // Cancel Ticket
    void cancelTicket(String pnr) {
        Booking booking = bookings.get(pnr);
        if (booking == null) {
            System.out.println("❌ Invalid PNR");
            return;
        }

        for (Passenger p : booking.passengers) {
            if (p.status.equals("Confirmed")) {
                freeSeat(p);
                confirmedCount--;
            } else if (p.status.equals("RAC")) {
                racList.remove(p);
                racCount--;
            } else if (p.status.equals("Waiting")) {
                waitingList.remove(p);
            }
        }

        bookings.remove(pnr);
        System.out.println("❌ Booking with PNR " + pnr + " cancelled.");

        upgradePassengers();
    }

    private void freeSeat(Passenger p) {
        for (List<Seat> coach : coaches) {
            for (Seat seat : coach) {
                if (seat.coachNo == p.coachNo && seat.cabinNo == p.cabinNo && seat.seatNo == p.seatNo) {
                    seat.isOccupied = false;
                    seat.passengerName = "";
                    return;
                }
            }
        }
    }

    // Upgrade passengers after cancellation
    private void upgradePassengers() {
        // RAC to Confirmed
        while (!racList.isEmpty() && confirmedCount < totalConfirmedSeats) {
            Passenger p = racList.poll();
            racCount--;
            allocateConfirmedSeat(p);
            System.out.println("✅ RAC passenger upgraded to Confirmed: " + p.name);
        }

        // Waiting to RAC
        while (!waitingList.isEmpty() && racCount < racLimit) {
            Passenger p = waitingList.poll();
            racList.add(p);
            racCount++;
            p.status = "RAC";
            System.out.println("✅ Waiting passenger moved to RAC: " + p.name);
        }
    }

    // Add cabin to existing coach
    void addCabin() {
        if (coaches.isEmpty()) {
            System.out.println("❌ No coaches available. Add a coach first.");
            return;
        }

        List<Seat> coach = coaches.get(0); // Add to first coach
        int currentCabins = (coach.size() / 8);
        int newCabinNo = currentCabins + 1;

        // Add 8 new seats to the cabin
        coach.add(new Seat(1, newCabinNo, 1, "Lower"));
        coach.add(new Seat(1, newCabinNo, 2, "Middle"));
        coach.add(new Seat(1, newCabinNo, 3, "Upper"));
        coach.add(new Seat(1, newCabinNo, 4, "Lower"));
        coach.add(new Seat(1, newCabinNo, 5, "Middle"));
        coach.add(new Seat(1, newCabinNo, 6, "Upper"));
        coach.add(new Seat(1, newCabinNo, 7, "SideLower"));
        coach.add(new Seat(1, newCabinNo, 8, "SideUpper"));

        totalConfirmedSeats += 8;
        System.out.println("✅ New cabin added. Total confirmed seats: " + totalConfirmedSeats);
    }

    // Add new coach
    void addCoach() {
        int newCoachNo = coaches.size() + 1;
        List<Seat> newCoach = new ArrayList<>();

        // Cabin 1
        for (int i = 1; i <= 8; i++) {
            String berthType = (i <= 3) ? (i == 1 ? "Lower" : i == 2 ? "Middle" : "Upper") :
                    (i <= 6) ? (i == 4 ? "Lower" : i == 5 ? "Middle" : "Upper") :
                            (i == 7 ? "SideLower" : "SideUpper");
            newCoach.add(new Seat(newCoachNo, 1, i, berthType));
        }

        // Cabin 2
        for (int i = 1; i <= 8; i++) {
            String berthType = (i <= 3) ? (i == 1 ? "Lower" : i == 2 ? "Middle" : "Upper") :
                    (i <= 6) ? (i == 4 ? "Lower" : i == 5 ? "Middle" : "Upper") :
                            (i == 7 ? "SideLower" : "SideUpper");
            newCoach.add(new Seat(newCoachNo, 2, i, berthType));
        }

        coaches.add(newCoach);
        totalConfirmedSeats += 16;
        System.out.println("✅ New coach added. Total confirmed seats: " + totalConfirmedSeats);
    }

    // Display available seats
    void showAvailableSeats() {
        System.out.println("\n🚂 Available Seats:");
        for (int i = 0; i < coaches.size(); i++) {
            List<Seat> coach = coaches.get(i);
            System.out.println("Coach " + (i + 1) + ":");

            for (Seat seat : coach) {
                String status = seat.isOccupied ? "❌seat is occupied" : "✅seat is free";
                System.out.println("  Cabin " + seat.cabinNo +
                        " Seat " + seat.seatNo +
                        " (" + seat.berthType + ") " + status);
            }
        }
        System.out.println("Confirmed: " + confirmedCount + "/" + totalConfirmedSeats +
                " | RAC: " + racCount + "/" + racLimit +
                " | Waiting: " + waitingList.size());
    }
}

//---------------- Main ----------------
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ReservationSystem rs = new ReservationSystem();

        while (true) {
            System.out.println("\n🚂 Railway Reservation System");
            System.out.println("1. Book Ticket");
            System.out.println("2. Show PNR Details");
            System.out.println("3. Cancel Ticket");
            System.out.println("4. Add Cabin");
            System.out.println("5. Add Coach");
            System.out.println("6. Show Available Seats");
            System.out.println("7. Exit");
            System.out.print("Choose: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Number of passengers: ");
                    int n = sc.nextInt();
                    List<Passenger> passengers = new ArrayList<>();

                    for (int i = 0; i < n; i++) {
                        System.out.println("Passenger " + (i + 1) + ":");
                        System.out.print("Name: ");
                        String name = sc.next();
                        System.out.print("Age: ");
                        int age = sc.nextInt();
                        System.out.print("Gender (M/F): ");
                        String gender = sc.next();
                        System.out.print("Berth Preference (L/M/U/SL/SU): ");
                        String pref = sc.next();

                        passengers.add(new Passenger(name, age, gender, pref));
                    }
                    rs.bookTicket(passengers);
                    break;

                case 2:
                    System.out.print("Enter PNR: ");
                    String pnr = sc.next();
                    rs.showPNR(pnr);
                    break;

                case 3:
                    System.out.print("Enter PNR to cancel: ");
                    String cancelPnr = sc.next();
                    rs.cancelTicket(cancelPnr);
                    break;

                case 4:
                    rs.addCabin();
                    break;

                case 5:
                    rs.addCoach();
                    break;

                case 6:
                    rs.showAvailableSeats();
                    break;

                case 7:
                    System.out.println("🚪 Thank you!");
                    sc.close();
                    return;

                default:
                    System.out.println("❌ Invalid choice");
            }
        }
    }
}