package cwrm.controller;

import cwrm.model.Material;
import cwrm.model.Resident;
import cwrm.model.TimeSlot;

/**
 * The raw values from the booking screen, exactly as the user entered them.
 * {@link PickupController#bookPickup(BookingForm)} checks every value.
 *
 * @param resident    the chosen resident, or {@code null} if none was chosen
 * @param addressText the address as typed
 * @param material    the chosen material, or {@code null} if none was chosen
 * @param dateText    the pickup date as typed; expected as {@code yyyy-MM-dd}
 * @param weightText  the estimated weight in kg as typed; may be blank (optional)
 * @param notes       notes for the collector; may be blank (optional)
 * @param timeSlot    the preferred time slot, or {@code null} for any time (optional)
 */
public record BookingForm(Resident resident, String addressText, Material material, String dateText,
                          String weightText, String notes, TimeSlot timeSlot) {
}
